package ru.semka.service

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.semka.domain.entity.WalletEntity
import ru.semka.domain.entity.WalletMemberEntity
import ru.semka.domain.enums.MemberRole
import ru.semka.dto.*
import ru.semka.exception.ApiException
import ru.semka.repository.*
import ru.semka.security.AppUserDetails
import java.math.BigDecimal

/**
 * сервис кошельков: список, CRUD, участники, баланс, приглашения.
 * координирует walletRepository, memberRepository, category/budget для «первого запуска» кошелька.
 */
@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val memberRepository: WalletMemberRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val access: WalletAccessService, // проверка прав
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // GET /wallets — только чтение; категории/бюджет по умолчанию — при открытии кошелька
    @Transactional(readOnly = true)
    fun listWallets(user: AppUserDetails): List<WalletDto> {
        val members = memberRepository.findByUserId(user.id)
        if (members.isEmpty()) return emptyList()
        val walletsById = walletRepository.findAllById(members.map { it.walletId }).associateBy { it.id!! }
        return members.mapNotNull { m ->
            val w = walletsById[m.walletId] ?: return@mapNotNull null
            toWalletDto(w, m)
        }.distinctBy { it.id }
    }

    // GET /wallets/{id}
    @Transactional
    fun getWallet(walletId: Long, user: AppUserDetails): WalletDto {
        val m = access.requireMember(walletId, user)
        val w = walletRepository.findById(walletId).orElseThrow { ApiException("NOT_FOUND", "кошелёк не найден") }
        ensureWalletReady(w)
        return toWalletDto(w, m)
    }

    // POST /wallets — создатель становится WALLET_OWNER с canSeeBudget=true
    @Transactional
    fun createWallet(req: CreateWalletRequest, user: AppUserDetails): WalletDto {
        val name = req.name.trim()
        if (name.isBlank()) throw ApiException("VALIDATION_ERROR", "укажите название кошелька")
        val wallet = walletRepository.saveAndFlush(WalletEntity(name = name, ownerId = user.id))
        val member = memberRepository.saveAndFlush(
            WalletMemberEntity(
                walletId = wallet.id!!,
                userId = user.id,
                memberRole = MemberRole.WALLET_OWNER,
                canSeeBudget = true,
            ),
        )
        ensureWalletReady(wallet)
        log.info("кошелёк создан: id={}, ownerId={}, name={}", wallet.id, user.id, wallet.name)
        return toWalletDto(wallet, member)
    }

    // GET /wallets/{id}/balance — кэш Redis 5 мин, ключ walletId
    @Cacheable(cacheNames = ["balance"], key = "#walletId")
    fun getBalance(walletId: Long, user: AppUserDetails): BalanceDto {
        access.requireMember(walletId, user)
        val income = transactionRepository.sumByWalletAndType(walletId, ru.semka.domain.enums.OperationType.INCOME).money()
        val expense = transactionRepository.sumByWalletAndType(walletId, ru.semka.domain.enums.OperationType.EXPENSE).money()
        return BalanceDto(income, expense, (income - expense).money())
    }

    // сброс кэша баланса (вызывается из TransactionService и здесь)
    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun evictBalance(walletId: Long) {}

    // GET /wallets/{id}/members
    fun listMembers(walletId: Long, user: AppUserDetails): List<MemberDto> {
        access.requireMember(walletId, user)
        return memberRepository.findByWalletId(walletId).map { m ->
            val nick = userRepository.findById(m.userId).map { it.nick }.orElse("?")
            m.toDto(nick)
        }
    }

    // POST /wallets/{id}/members — только владелец
    @Transactional
    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun invite(walletId: Long, nick: String, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        val invited = userRepository.findByNick(nick.trim())
            .orElseThrow { ApiException("NOT_FOUND", "ник не найден — нужна регистрация") }
        if (memberRepository.findByWalletIdAndUserId(walletId, invited.id!!).isPresent) {
            throw ApiException("CONFLICT", "уже в кошельке")
        }
        memberRepository.save(
            WalletMemberEntity(
                walletId = walletId,
                userId = invited.id!!,
                memberRole = MemberRole.WALLET_MEMBER,
                canSeeBudget = false,
            ),
        )
    }

    // PUT /wallets/{id} — переименование
    @Transactional
    fun update(walletId: Long, req: UpdateWalletRequest, user: AppUserDetails): WalletDto {
        access.requireOwner(walletId, user)
        val w = walletRepository.findById(walletId).orElseThrow { ApiException("NOT_FOUND", "кошелёк не найден") }
        w.name = req.name.trim()
        walletRepository.save(w)
        return getWallet(walletId, user)
    }

    // DELETE /wallets/{id} — каскадно удалятся операции, участники (Flyway FK)
    @Transactional
    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun deleteWallet(walletId: Long, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        if (!walletRepository.existsById(walletId)) {
            throw ApiException("NOT_FOUND", "кошелёк не найден")
        }
        walletRepository.deleteById(walletId)
    }

    // DELETE /wallets/{id}/members/{memberId}
    @Transactional
    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun removeMember(walletId: Long, memberId: Long, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        val m = memberRepository.findById(memberId).orElseThrow { ApiException("NOT_FOUND", "участник не найден") }
        if (m.walletId != walletId) throw ApiException("NOT_FOUND", "участник не найден")
        if (m.memberRole == MemberRole.WALLET_OWNER) {
            throw ApiException("VALIDATION_ERROR", "нельзя удалить владельца")
        }
        memberRepository.delete(m)
    }

    // POST /wallets/{id}/leave — участник выходит сам
    @Transactional
    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun leaveWallet(walletId: Long, user: AppUserDetails) {
        val m = access.requireMember(walletId, user)
        if (m.memberRole == MemberRole.WALLET_OWNER) {
            throw ApiException(
                "VALIDATION_ERROR",
                "владелец не может выйти — удалите кошелёк или передайте владение",
            )
        }
        memberRepository.delete(m)
    }

    // PATCH /wallets/{id}/members/{memberId} — флаг canSeeBudget
    @Transactional
    fun setCanSeeBudget(walletId: Long, memberId: Long, canSee: Boolean, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        val m = memberRepository.findById(memberId).orElseThrow { ApiException("NOT_FOUND", "участник не найден") }
        if (m.walletId != walletId) throw ApiException("NOT_FOUND", "участник не найден")
        if (m.memberRole == MemberRole.WALLET_OWNER) return
        m.canSeeBudget = canSee
        memberRepository.save(m)
    }

    // если в кошельке нет категорий — создать шаблоны и пустой бюджет месяца
    private fun ensureWalletReady(w: WalletEntity) {
        val wid = w.id!!
        if (categoryRepository.findByWalletId(wid).isNotEmpty()) return
        categoryRepository.saveAll(createDefaultCategories(wid, w.ownerId))
        val (from, to) = currentMonthPeriod()
        if (budgetRepository.findByWalletIdAndCategoryIdAndPeriodStart(wid, null, from).isEmpty) {
            budgetRepository.save(
                ru.semka.domain.entity.BudgetEntity(
                    walletId = wid,
                    categoryId = null,
                    periodStart = from,
                    periodEnd = to,
                    limitAmount = BigDecimal.ZERO,
                ),
            )
        }
        evictBalance(wid)
    }

    // Entity + membership → WalletDto с опциональными лимитами бюджета
    private fun toWalletDto(w: WalletEntity, m: WalletMemberEntity): WalletDto {
        val canSee = m.memberRole == MemberRole.WALLET_OWNER || m.canSeeBudget
        var limit: BigDecimal? = null
        var remaining: BigDecimal? = null
        if (canSee) {
            val (from, to) = currentMonthPeriod()
            val overall = budgetRepository.findByWalletIdAndCategoryIdAndPeriodStart(w.id!!, null, from)
            if (overall.isPresent && overall.get().limitAmount > BigDecimal.ZERO) {
                val b = overall.get()
                val spent = transactionRepository.sumExpenseByWalletAndPeriod(w.id!!, from, to).money()
                limit = b.limitAmount.money()
                remaining = (b.limitAmount - spent).money()
            }
        }
        return WalletDto(w.id!!, w.name, m.memberRole, canSee, limit, remaining)
    }
}
