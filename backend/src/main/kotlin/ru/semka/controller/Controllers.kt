package ru.semka.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.semka.domain.enums.OperationType
import ru.semka.dto.*
import ru.semka.security.AppUserDetails
import ru.semka.service.*

/**
 * все REST-контроллеры проекта в одном файле (для курсовой — проще искать).
 * префикс API: /api (задаётся в application.yml → context-path).
 *
 * правило слоёв: контроллер только принимает HTTP и вызывает service.
 * бизнес-логика — в service, работа с БД — в repository.
 *
 * @AuthenticationPrincipal user — текущий пользователь из JWT (после фильтра JwtAuthFilter).
 */

// ——— авторизация: регистрация, вход, профиль, удаление аккаунта ———

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    /** POST /auth/register — новый пользователь: email, nick, password. сразу выдаёт JWT. */
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest) = authService.register(req)

    /** POST /auth/login — проверка email/пароля, в ответе token + данные user. */
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = authService.login(req)

    /** GET /auth/me — кто залогинен (нужен заголовок Authorization: Bearer …). */
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: AppUserDetails) = authService.me(user)

    /** DELETE /auth/me — удалить свой аккаунт; в теле обязателен password для подтверждения. */
    @DeleteMapping("/me")
    fun deleteAccount(
        @Valid @RequestBody req: DeleteAccountRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        authService.deleteAccount(req, user)
    }
}

// ——— кошельки: список, создание, участники, баланс, выход ———

@RestController
@RequestMapping("/wallets")
class WalletController(private val walletService: WalletService) {

    /** GET /wallets — все кошельки, где пользователь участник (владелец или member). */
    @GetMapping
    fun list(@AuthenticationPrincipal user: AppUserDetails) = walletService.listWallets(user)

    /** POST /wallets — создать кошелёк; тело: { "name": "Семья" }. создатель становится владельцем. */
    @PostMapping
    fun create(@Valid @RequestBody req: CreateWalletRequest, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.createWallet(req, user)

    /** GET /wallets/{id} — один кошелёк: название, моя роль, бюджет (если canSeeBudget). */
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.getWallet(id, user)

    /** PUT /wallets/{id} — переименовать кошелёк (только владелец). */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateWalletRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = walletService.update(id, req, user)

    /** DELETE /wallets/{id} — удалить кошелёк и всё содержимое (только владелец). */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        walletService.deleteWallet(id, user)
    }

    /**
     * GET /wallets/{id}/balance — доход, расход и баланс за всё время кошелька.
     * баланс не хранится в таблице — считается суммой операций; результат кэшируется в Redis.
     */
    @GetMapping("/{id}/balance")
    fun balance(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.getBalance(id, user)

    /** GET /wallets/{id}/members — список участников с никами и флагом canSeeBudget. */
    @GetMapping("/{id}/members")
    fun members(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.listMembers(id, user)

    /** POST /wallets/{id}/members — пригласить по нику: { "nick": "mama" }. пользователь должен быть зарегистрирован. */
    @PostMapping("/{id}/members")
    fun invite(
        @PathVariable id: Long,
        @Valid @RequestBody req: InviteMemberRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        walletService.invite(id, req.nick, user)
    }

    /** PATCH /wallets/{id}/members/{memberId} — владелец вкл/выкл показ бюджета: { "canSeeBudget": true }. */
    @PatchMapping("/{id}/members/{memberId}")
    fun patchMember(
        @PathVariable id: Long,
        @PathVariable memberId: Long,
        @Valid @RequestBody req: PatchMemberRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        walletService.setCanSeeBudget(id, memberId, req.canSeeBudget, user)
    }

    /** DELETE /wallets/{id}/members/{memberId} — владелец удаляет участника из кошелька (не себя). */
    @DeleteMapping("/{id}/members/{memberId}")
    fun removeMember(
        @PathVariable id: Long,
        @PathVariable memberId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        walletService.removeMember(id, memberId, user)
    }

    /** POST /wallets/{id}/leave — участник выходит из кошелька; владелец не может — только удалить кошелёк. */
    @PostMapping("/{id}/leave")
    fun leave(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        walletService.leaveWallet(id, user)
    }
}

// ——— категории доходов и расходов внутри кошелька ———

@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {

    /**
     * GET /categories?walletId=1&tip=EXPENSE — список категорий кошелька.
     * tip необязателен: INCOME или EXPENSE; без tip — все категории.
     */
    @GetMapping
    fun list(
        @RequestParam walletId: Long,
        @RequestParam(required = false) tip: OperationType?,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = categoryService.list(walletId, tip, user)

    /** GET /categories/{id} — одна категория по id. */
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        categoryService.get(id, user)

    /** POST /categories — создать категорию (только владелец): имя, тип, иконка, цвет. */
    @PostMapping
    fun create(@Valid @RequestBody req: CreateCategoryRequest, @AuthenticationPrincipal user: AppUserDetails) =
        categoryService.create(req, user)

    /** PUT /categories/{id} — изменить название, иконку, цвет (владелец). */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateCategoryRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = categoryService.update(id, req, user)

    /** DELETE /categories/{id} — удалить, если нет операций с этой категорией (владелец). */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        categoryService.delete(id, user)
    }
}

// ——— операции: доходы, расходы, split (разделить чек) ———

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {

    /**
     * GET /transactions?walletId=1&tip=EXPENSE — список операций кошелька, новые сверху.
     * tip — фильтр по INCOME или EXPENSE.
     */
    @GetMapping
    fun list(
        @RequestParam walletId: Long,
        @RequestParam(required = false) tip: OperationType?,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = transactionService.list(walletId, tip, user)

    /** GET /transactions/{id} — одна операция с автором и категорией. */
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        transactionService.get(id, user)

    /** POST /transactions — новая операция: кошелёк, категория, тип, сумма, дата, комментарий. */
    @PostMapping
    fun create(@Valid @RequestBody req: CreateTransactionRequest, @AuthenticationPrincipal user: AppUserDetails) =
        transactionService.create(req, user)

    /** PUT /transactions/{id} — изменить операцию; участник — только свою, владелец — любую. */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateTransactionRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = transactionService.update(id, req, user)

    /** DELETE /transactions/{id} — удалить операцию (те же правила, что и для изменения). */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        transactionService.delete(id, user)
    }

    /**
     * POST /transactions/split — один общий расход + доли участников.
     * сумма долей должна совпасть с totalAmount (допуск 0.02 ₽).
     */
    @PostMapping("/split")
    fun split(@Valid @RequestBody req: SplitRequest, @AuthenticationPrincipal user: AppUserDetails) {
        transactionService.split(req, user)
    }
}

// ——— бюджеты на текущий месяц (общий и по категориям) ———

@RestController
@RequestMapping("/budgets")
class BudgetController(private val budgetService: BudgetService) {

    /**
     * GET /budgets?walletId=1 — бюджеты кошелька за текущий месяц.
     * участник видит список только если canSeeBudget или он владелец.
     */
    @GetMapping
    fun list(@RequestParam walletId: Long, @AuthenticationPrincipal user: AppUserDetails) =
        budgetService.list(walletId, user)

    /** POST /budgets — задать/обновить лимит (upsert): walletId, categoryId (null = весь кошелёк), limitAmount. */
    @PostMapping
    fun create(@Valid @RequestBody req: UpsertBudgetRequest, @AuthenticationPrincipal user: AppUserDetails) =
        budgetService.upsert(req, user)

    /** PUT /budgets — то же, что POST (сохранение лимита на период). */
    @PutMapping
    fun upsert(@Valid @RequestBody req: UpsertBudgetRequest, @AuthenticationPrincipal user: AppUserDetails) =
        budgetService.upsert(req, user)

    /** DELETE /budgets/{id}?walletId=1 — убрать лимит по записи бюджета (владелец). */
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @RequestParam walletId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        budgetService.delete(walletId, id, user)
    }
}

// ——— отчёты за период ———

@RestController
@RequestMapping("/reports")
class ReportController(private val reportService: ReportService) {

    /**
     * GET /reports/period?walletId=1&from=2026-05-01&to=2026-05-31
     * сводка: сумма доходов, расходов, разбивка расходов по категориям.
     */
    @GetMapping("/period")
    fun period(
        @RequestParam walletId: Long,
        @RequestParam from: String,
        @RequestParam to: String,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = reportService.periodReport(walletId, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to), user)

    /**
     * GET /reports/by-member?walletId=1&from=…&to=…
     * сколько каждый участник потратил (по author_id операций) за период.
     */
    @GetMapping("/by-member")
    fun byMember(
        @RequestParam walletId: Long,
        @RequestParam from: String,
        @RequestParam to: String,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = reportService.expensesByMember(walletId, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to), user)
}

// ——— автоплатежи «раз в месяц» (только владелец) ———

@RestController
@RequestMapping("/recurring-rules")
class RecurringController(private val recurringService: RecurringService) {

    /** GET /recurring-rules?walletId=1 — все правила автоплатежей кошелька. */
    @GetMapping
    fun list(@RequestParam walletId: Long, @AuthenticationPrincipal user: AppUserDetails) =
        recurringService.list(walletId, user)

    /** GET /recurring-rules/{id}?walletId=1 — одно правило. */
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        @RequestParam walletId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = recurringService.get(id, walletId, user)

    /** POST /recurring-rules — создать: категория, сумма, день месяца (1–31), комментарий. */
    @PostMapping
    fun create(@Valid @RequestBody req: CreateRecurringRequest, @AuthenticationPrincipal user: AppUserDetails) =
        recurringService.create(req, user)

    /** PUT /recurring-rules/{id} — изменить правило. */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateRecurringRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = recurringService.update(id, req, user)

    /** DELETE /recurring-rules/{id}?walletId=1 — удалить правило. */
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @RequestParam walletId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        recurringService.delete(id, walletId, user)
    }
}
