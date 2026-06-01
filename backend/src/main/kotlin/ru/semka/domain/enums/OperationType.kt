package ru.semka.domain.enums

/**
 * тип финансовой операции и категории: доход или расход.
 * хранится строкой в БД (INCOME / EXPENSE), совпадает с TipOper на фронте.
 */
enum class OperationType {
    INCOME, // поступление денег (зарплата, возврат)
    EXPENSE, // трата (продукты, коммуналка)
}
