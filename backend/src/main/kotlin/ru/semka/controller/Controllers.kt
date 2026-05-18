package ru.semka.controller

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.semka.domain.enums.OperationType
import ru.semka.dto.*
import ru.semka.security.AppUserDetails
import ru.semka.service.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest) = authService.register(req)

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = authService.login(req)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: AppUserDetails) = authService.me(user)

    @DeleteMapping("/me")
    fun deleteAccount(
        @Valid @RequestBody req: DeleteAccountRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        authService.deleteAccount(req, user)
    }
}

@RestController
@RequestMapping("/wallets")
class WalletController(private val walletService: WalletService) {
    @GetMapping
    fun list(@AuthenticationPrincipal user: AppUserDetails) = walletService.listWallets(user)

    @PostMapping
    fun create(@Valid @RequestBody req: CreateWalletRequest, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.createWallet(req, user)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.getWallet(id, user)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateWalletRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = walletService.update(id, req, user)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        walletService.deleteWallet(id, user)
    }

    @GetMapping("/{id}/balance")
    fun balance(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.getBalance(id, user)

    @GetMapping("/{id}/members")
    fun members(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        walletService.listMembers(id, user)

    @PostMapping("/{id}/members")
    fun invite(
        @PathVariable id: Long,
        @Valid @RequestBody req: InviteMemberRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        walletService.invite(id, req.nick, user)
    }

    @PatchMapping("/{id}/members/{memberId}")
    fun patchMember(
        @PathVariable id: Long,
        @PathVariable memberId: Long,
        @Valid @RequestBody req: PatchMemberRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        walletService.setCanSeeBudget(id, memberId, req.canSeeBudget, user)
    }

    @DeleteMapping("/{id}/members/{memberId}")
    fun removeMember(
        @PathVariable id: Long,
        @PathVariable memberId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        walletService.removeMember(id, memberId, user)
    }

    @PostMapping("/{id}/leave")
    fun leave(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        walletService.leaveWallet(id, user)
    }
}

@RestController
@RequestMapping("/categories")
class CategoryController(private val categoryService: CategoryService) {
    @GetMapping
    fun list(
        @RequestParam walletId: Long,
        @RequestParam(required = false) tip: OperationType?,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = categoryService.list(walletId, tip, user)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        categoryService.get(id, user)

    @PostMapping
    fun create(@Valid @RequestBody req: CreateCategoryRequest, @AuthenticationPrincipal user: AppUserDetails) =
        categoryService.create(req, user)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateCategoryRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = categoryService.update(id, req, user)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        categoryService.delete(id, user)
    }
}

@RestController
@RequestMapping("/transactions")
class TransactionController(private val transactionService: TransactionService) {
    @GetMapping
    fun list(
        @RequestParam walletId: Long,
        @RequestParam(required = false) tip: OperationType?,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = transactionService.list(walletId, tip, user)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) =
        transactionService.get(id, user)

    @PostMapping
    fun create(@Valid @RequestBody req: CreateTransactionRequest, @AuthenticationPrincipal user: AppUserDetails) =
        transactionService.create(req, user)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateTransactionRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = transactionService.update(id, req, user)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal user: AppUserDetails) {
        transactionService.delete(id, user)
    }

    @PostMapping("/split")
    fun split(@Valid @RequestBody req: SplitRequest, @AuthenticationPrincipal user: AppUserDetails) {
        transactionService.split(req, user)
    }
}

@RestController
@RequestMapping("/budgets")
class BudgetController(private val budgetService: BudgetService) {
    @GetMapping
    fun list(@RequestParam walletId: Long, @AuthenticationPrincipal user: AppUserDetails) =
        budgetService.list(walletId, user)

    @PostMapping
    fun create(@Valid @RequestBody req: UpsertBudgetRequest, @AuthenticationPrincipal user: AppUserDetails) =
        budgetService.upsert(req, user)

    @PutMapping
    fun upsert(@Valid @RequestBody req: UpsertBudgetRequest, @AuthenticationPrincipal user: AppUserDetails) =
        budgetService.upsert(req, user)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @RequestParam walletId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        budgetService.delete(walletId, id, user)
    }
}

@RestController
@RequestMapping("/reports")
class ReportController(private val reportService: ReportService) {
    @GetMapping("/period")
    fun period(
        @RequestParam walletId: Long,
        @RequestParam from: String,
        @RequestParam to: String,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = reportService.periodReport(walletId, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to), user)

    @GetMapping("/by-member")
    fun byMember(
        @RequestParam walletId: Long,
        @RequestParam from: String,
        @RequestParam to: String,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = reportService.expensesByMember(walletId, java.time.LocalDate.parse(from), java.time.LocalDate.parse(to), user)
}

@RestController
@RequestMapping("/recurring-rules")
class RecurringController(private val recurringService: RecurringService) {
    @GetMapping
    fun list(@RequestParam walletId: Long, @AuthenticationPrincipal user: AppUserDetails) =
        recurringService.list(walletId, user)

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        @RequestParam walletId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = recurringService.get(id, walletId, user)

    @PostMapping
    fun create(@Valid @RequestBody req: CreateRecurringRequest, @AuthenticationPrincipal user: AppUserDetails) =
        recurringService.create(req, user)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody req: UpdateRecurringRequest,
        @AuthenticationPrincipal user: AppUserDetails,
    ) = recurringService.update(id, req, user)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @RequestParam walletId: Long,
        @AuthenticationPrincipal user: AppUserDetails,
    ) {
        recurringService.delete(id, walletId, user)
    }
}
