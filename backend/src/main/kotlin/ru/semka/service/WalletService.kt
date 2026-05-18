package ru.semka.service

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

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val memberRepository: WalletMemberRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val access: WalletAccessService,
) {
    @Transactional
    fun listWallets(user: AppUserDetails): List<WalletDto> {
        cleanupDuplicateEmptyOwnedWallets(user.id)
        return memberRepository.findByUserId(user.id)
            .mapNotNull { m ->
                val w = walletRepository.findById(m.walletId).orElse(null) ?: return@mapNotNull null
                ensureWalletReady(w)
                toWalletDto(w, m)
            }
            .distinctBy { it.id }
    }

    @Transactional
    fun getWallet(walletId: Long, user: AppUserDetails): WalletDto {
        val m = access.requireMember(walletId, user)
        val w = walletRepository.findById(walletId).orElseThrow { ApiException("NOT_FOUND", "кошелёк не найден") }
        ensureWalletReady(w)
        return toWalletDto(w, m)
    }

    @Transactional
    fun createWallet(req: CreateWalletRequest, user: AppUserDetails): WalletDto {
        val name = req.name.trim()
        if (name.isBlank()) throw ApiException("VALIDATION_ERROR", "укажите название кошелька")
        val wallet = walletRepository.save(WalletEntity(name = name, ownerId = user.id))
        memberRepository.save(
            WalletMemberEntity(
                walletId = wallet.id!!,
                userId = user.id,
                memberRole = MemberRole.WALLET_OWNER,
                canSeeBudget = true,
            ),
        )
        ensureWalletReady(wallet)
        return getWallet(wallet.id!!, user)
    }

    @Cacheable(cacheNames = ["balance"], key = "#walletId")
    fun getBalance(walletId: Long, user: AppUserDetails): BalanceDto {
        access.requireMember(walletId, user)
        val income = transactionRepository.sumByWalletAndType(walletId, ru.semka.domain.enums.OperationType.INCOME).money()
        val expense = transactionRepository.sumByWalletAndType(walletId, ru.semka.domain.enums.OperationType.EXPENSE).money()
        return BalanceDto(income, expense, (income - expense).money())
    }

    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun evictBalance(walletId: Long) {}

    fun listMembers(walletId: Long, user: AppUserDetails): List<MemberDto> {
        access.requireMember(walletId, user)
        return memberRepository.findByWalletId(walletId).map { m ->
            val nick = userRepository.findById(m.userId).map { it.nick }.orElse("?")
            m.toDto(nick)
        }
    }

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

    @Transactional
    fun update(walletId: Long, req: UpdateWalletRequest, user: AppUserDetails): WalletDto {
        access.requireOwner(walletId, user)
        val w = walletRepository.findById(walletId).orElseThrow { ApiException("NOT_FOUND", "кошелёк не найден") }
        w.name = req.name.trim()
        walletRepository.save(w)
        return getWallet(walletId, user)
    }

    @Transactional
    @CacheEvict(cacheNames = ["balance"], key = "#walletId")
    fun deleteWallet(walletId: Long, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        if (!walletRepository.existsById(walletId)) {
            throw ApiException("NOT_FOUND", "кошелёк не найден")
        }
        walletRepository.deleteById(walletId)
    }

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

    @Transactional
    fun setCanSeeBudget(walletId: Long, memberId: Long, canSee: Boolean, user: AppUserDetails) {
        access.requireOwner(walletId, user)
        val m = memberRepository.findById(memberId).orElseThrow { ApiException("NOT_FOUND", "участник не найден") }
        if (m.walletId != walletId) throw ApiException("NOT_FOUND", "участник не найден")
        if (m.memberRole == MemberRole.WALLET_OWNER) return
        m.canSeeBudget = canSee
        memberRepository.save(m)
    }

    /** Удаляем пустые дубли: если есть «живой» кошелёк — все пустые; иначе оставляем один пустой. */
    private fun cleanupDuplicateEmptyOwnedWallets(userId: Long) {
        val owned = walletRepository.findByOwnerId(userId)
        val emptyOwned = owned
            .filter { transactionRepository.countByWalletId(it.id!!) == 0L }
            .sortedBy { it.id }
        if (emptyOwned.isEmpty()) return
        val hasActive = owned.any { transactionRepository.countByWalletId(it.id!!) > 0L }
        val toDelete = if (hasActive) emptyOwned else emptyOwned.drop(1)
        toDelete.forEach { walletRepository.deleteById(it.id!!) }
    }

    /** Пустой кошелёк без категорий — дополняем дефолтами (после сбоев или старых тестов). */
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
