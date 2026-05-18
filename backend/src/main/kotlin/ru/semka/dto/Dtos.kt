package ru.semka.dto

import jakarta.validation.constraints.*
import ru.semka.domain.enums.AppRole
import ru.semka.domain.enums.MemberRole
import ru.semka.domain.enums.OperationType
import java.math.BigDecimal
import java.time.LocalDate

data class ErrorDto(
    val code: String,
    val message: String,
    val details: List<String> = emptyList(),
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)

data class DeleteAccountRequest(
    @field:NotBlank val password: String,
)

data class RegisterRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank @field:Size(max = 32) val nick: String,
)

data class UserDto(
    val id: Long,
    val email: String,
    val nick: String,
    val role: AppRole,
)

data class LoginResponse(
    val token: String,
    val user: UserDto,
)

data class WalletDto(
    val id: Long,
    val name: String,
    val myRole: MemberRole,
    val canSeeBudget: Boolean,
    val budgetLimit: BigDecimal? = null,
    val budgetRemaining: BigDecimal? = null,
)

data class CreateWalletRequest(
    @field:NotBlank val name: String,
)

data class UpdateWalletRequest(
    @field:NotBlank val name: String,
)

data class BalanceDto(
    val income: BigDecimal,
    val expense: BigDecimal,
    val balance: BigDecimal,
)

data class MemberDto(
    val id: Long,
    val userId: Long,
    val nick: String,
    val memberRole: MemberRole,
    val canSeeBudget: Boolean,
)

data class InviteMemberRequest(
    @field:NotBlank val nick: String,
)

data class PatchMemberRequest(
    val canSeeBudget: Boolean,
)

data class CategoryDto(
    val id: Long,
    val walletId: Long,
    val name: String,
    val tip: OperationType,
    val iconKey: String,
    val colorBg: String,
)

data class CreateCategoryRequest(
    @field:NotNull val walletId: Long,
    @field:NotBlank val name: String,
    @field:NotNull val tip: OperationType,
    val iconKey: String = "cart",
    val colorBg: String = "#AAF0D1",
)

data class UpdateCategoryRequest(
    @field:NotBlank val name: String,
    val iconKey: String = "cart",
    val colorBg: String = "#AAF0D1",
)

data class SplitShareDto(
    val nick: String,
    val shareAmount: BigDecimal,
)

data class TransactionDto(
    val id: Long,
    val walletId: Long,
    val authorId: Long,
    val authorNick: String,
    val categoryId: Long,
    val categoryName: String,
    val type: OperationType,
    val amount: BigDecimal,
    val transactionDate: LocalDate,
    val comment: String? = null,
    val splitDoli: List<SplitShareDto>? = null,
)

data class CreateTransactionRequest(
    @field:NotNull val walletId: Long,
    @field:NotNull val categoryId: Long,
    @field:NotNull val type: OperationType,
    @field:NotNull @field:DecimalMin("0.01") val amount: BigDecimal,
    @field:NotNull val transactionDate: LocalDate,
    val comment: String? = null,
)

data class UpdateTransactionRequest(
    @field:NotNull val categoryId: Long,
    @field:NotNull val type: OperationType,
    @field:NotNull @field:DecimalMin("0.01") val amount: BigDecimal,
    @field:NotNull val transactionDate: LocalDate,
    val comment: String? = null,
)

data class ShareInputDto(
    @field:NotNull val userId: Long,
    @field:NotNull @field:DecimalMin("0.01") val shareAmount: BigDecimal,
)

data class SplitRequest(
    @field:NotNull val walletId: Long,
    @field:NotNull val categoryId: Long,
    @field:NotNull @field:DecimalMin("0.01") val totalAmount: BigDecimal,
    @field:NotNull val transactionDate: LocalDate,
    val comment: String? = null,
    @field:NotEmpty val shares: List<ShareInputDto>,
)

data class BudgetDto(
    val id: Long,
    val walletId: Long,
    val categoryId: Long?,
    val categoryName: String? = null,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val limitAmount: BigDecimal,
    val spent: BigDecimal? = null,
    val remaining: BigDecimal? = null,
)

data class UpsertBudgetRequest(
    @field:NotNull val walletId: Long,
    val categoryId: Long? = null,
    @field:NotNull @field:DecimalMin("0") val limitAmount: BigDecimal,
)

data class CategoryExpenseDto(
    val categoryId: Long,
    val categoryName: String,
    val expense: BigDecimal,
)

data class PeriodReportDto(
    val walletId: Long,
    val from: LocalDate,
    val to: LocalDate,
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val byCategory: List<CategoryExpenseDto>,
)

data class MemberExpenseDto(
    val userId: Long,
    val nick: String,
    val expense: BigDecimal,
)

data class RecurringRuleDto(
    val id: Long,
    val walletId: Long,
    val categoryId: Long,
    val categoryName: String? = null,
    val amount: BigDecimal,
    val dayOfMonth: Int,
    val nextRunDate: LocalDate,
    val active: Boolean,
    val comment: String? = null,
)

data class CreateRecurringRequest(
    @field:NotNull val walletId: Long,
    @field:NotNull val categoryId: Long,
    @field:NotNull @field:DecimalMin("0.01") val amount: BigDecimal,
    @field:Min(1) @field:Max(31) val dayOfMonth: Int,
    @field:NotNull val nextRunDate: LocalDate,
    val active: Boolean = true,
    val comment: String? = null,
)

data class UpdateRecurringRequest(
    @field:NotNull val walletId: Long,
    @field:NotNull val categoryId: Long,
    @field:NotNull @field:DecimalMin("0.01") val amount: BigDecimal,
    @field:Min(1) @field:Max(31) val dayOfMonth: Int,
    @field:NotNull val nextRunDate: LocalDate,
    val active: Boolean = true,
    val comment: String? = null,
)
