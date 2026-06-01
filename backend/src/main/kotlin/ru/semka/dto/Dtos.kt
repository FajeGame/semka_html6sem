package ru.semka.dto

import jakarta.validation.constraints.*
import ru.semka.domain.enums.AppRole
import ru.semka.domain.enums.MemberRole
import ru.semka.domain.enums.OperationType
import java.math.BigDecimal
import java.time.LocalDate

/**
 * все DTO проекта — JSON тела запросов и ответов REST API.
 * не мапятся напрямую на таблицы: собираются в сервисах из Entity + вычислений.
 * @field:NotBlank и др. — автопроверка в контроллере до вызова service.
 */

// ——— ошибки и авторизация ———

/** ответ при любой ошибке API (единый формат для фронта) */
data class ErrorDto(
    val code: String, // машинный код: VALIDATION_ERROR, FORBIDDEN, …
    val message: String, // текст для пользователя
    val details: List<String> = emptyList(), // список полей при валидации
)

/** тело POST /auth/login */
data class LoginRequest(
    @field:NotBlank val email: String, // email пользователя
    @field:NotBlank val password: String, // пароль в открытом виде (только по HTTPS)
)

/** тело DELETE /auth/me */
data class DeleteAccountRequest(
    @field:NotBlank val password: String, // подтверждение перед удалением аккаунта
)

/** тело POST /auth/register */
data class RegisterRequest(
    @field:NotBlank val email: String, // новый email
    @field:NotBlank val password: String, // пароль
    @field:NotBlank @field:Size(max = 32) val nick: String, // ник для приглашений
)

/** пользователь в ответах (без пароля) */
data class UserDto(
    val id: Long, // id в БД
    val email: String,
    val nick: String,
    val role: AppRole, // USER или ADMIN
)

/** ответ login/register: токен + профиль */
data class LoginResponse(
    val token: String, // JWT для заголовка Authorization
    val user: UserDto,
)

// ——— кошельки и участники ———

/** карточка кошелька в списке и на странице */
data class WalletDto(
    val id: Long,
    val name: String, // название кошелька
    val myRole: MemberRole, // моя роль в этом кошельке
    val canSeeBudget: Boolean, // могу ли видеть бюджет (участник)
    val budgetLimit: BigDecimal? = null, // общий лимит месяца (если разрешено показывать)
    val budgetRemaining: BigDecimal? = null, // остаток общего лимита
)

/** тело POST /wallets */
data class CreateWalletRequest(
    @field:NotBlank val name: String, // имя нового кошелька
)

/** тело PUT /wallets/{id} */
data class UpdateWalletRequest(
    @field:NotBlank val name: String, // новое имя
)

/** GET /wallets/{id}/balance */
data class BalanceDto(
    val income: BigDecimal, // сумма всех INCOME
    val expense: BigDecimal, // сумма всех EXPENSE
    val balance: BigDecimal, // income − expense
)

/** один участник кошелька */
data class MemberDto(
    val id: Long, // id строки wallet_members
    val userId: Long, // id пользователя
    val nick: String, // отображаемый ник
    val memberRole: MemberRole,
    val canSeeBudget: Boolean,
)

/** тело POST /wallets/{id}/members */
data class InviteMemberRequest(
    @field:NotBlank val nick: String, // ник уже зарегистрированного пользователя
)

/** тело PATCH /wallets/{id}/members/{memberId} */
data class PatchMemberRequest(
    val canSeeBudget: Boolean, // вкл/выкл показ бюджета участнику
)

// ——— категории ———

data class CategoryDto(
    val id: Long,
    val walletId: Long,
    val name: String,
    val tip: OperationType, // INCOME или EXPENSE
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

// ——— операции и split ———

/** доля в split для отображения в списке операций */
data class SplitShareDto(
    val nick: String, // ник участника
    val shareAmount: BigDecimal, // его доля в рублях
)

data class TransactionDto(
    val id: Long,
    val walletId: Long,
    val authorId: Long,
    val authorNick: String, // кто создал запись
    val categoryId: Long,
    val categoryName: String,
    val type: OperationType,
    val amount: BigDecimal,
    val transactionDate: LocalDate,
    val comment: String? = null,
    val splitDoli: List<SplitShareDto>? = null, // заполнено только у split-операций
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

/** одна доля в теле POST /transactions/split */
data class ShareInputDto(
    @field:NotNull val userId: Long,
    @field:NotNull @field:DecimalMin("0.01") val shareAmount: BigDecimal,
)

data class SplitRequest(
    @field:NotNull val walletId: Long,
    @field:NotNull val categoryId: Long,
    @field:NotNull @field:DecimalMin("0.01") val totalAmount: BigDecimal, // общая сумма чека
    @field:NotNull val transactionDate: LocalDate,
    val comment: String? = null,
    @field:NotEmpty val shares: List<ShareInputDto>, // доли участников
)

// ——— бюджеты ———

data class BudgetDto(
    val id: Long,
    val walletId: Long,
    val categoryId: Long?, // null = лимит на весь кошелёк
    val categoryName: String? = null,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val limitAmount: BigDecimal, // лимит
    val spent: BigDecimal? = null, // уже потрачено за период
    val remaining: BigDecimal? = null, // limit − spent
)

data class UpsertBudgetRequest(
    @field:NotNull val walletId: Long,
    val categoryId: Long? = null,
    @field:NotNull @field:DecimalMin("0") val limitAmount: BigDecimal,
)

// ——— отчёты ———

data class CategoryExpenseDto(
    val categoryId: Long,
    val categoryName: String,
    val expense: BigDecimal, // сумма расходов по категории за период
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
    val expense: BigDecimal, // сколько потратил участник (по author_id)
)

// ——— автоплатежи ———

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
