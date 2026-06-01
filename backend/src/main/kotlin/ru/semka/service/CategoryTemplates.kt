package ru.semka.service

import ru.semka.domain.entity.CategoryEntity
import ru.semka.domain.enums.OperationType

/**
 * шаблоны категорий при первом открытии пустого кошелька (ensureWalletReady).
 * не хранятся в БД отдельно — копируются в таблицу categories.
 */

// одна строка шаблона
data class CatTemplate(
    val name: String, // название
    val tip: OperationType, // INCOME или EXPENSE
    val iconKey: String,
    val colorBg: String,
)

// набор по умолчанию для нового кошелька
val defaultCategories = listOf(
    CatTemplate("Продукты", OperationType.EXPENSE, "cart", "#d8f3e4"),
    CatTemplate("Транспорт", OperationType.EXPENSE, "car", "#cce8f8"),
    CatTemplate("Дом", OperationType.EXPENSE, "home", "#f5e6d3"),
    CatTemplate("Зарплата", OperationType.INCOME, "wallet", "#AAF0D1"),
    CatTemplate("Подарки", OperationType.INCOME, "gift", "#d1c4e9"),
)

// превратить шаблоны в сущности для saveAll
fun createDefaultCategories(walletId: Long, createdBy: Long): List<CategoryEntity> =
    defaultCategories.map {
        CategoryEntity(
            walletId = walletId,
            name = it.name,
            tip = it.tip,
            iconKey = it.iconKey,
            colorBg = it.colorBg,
            createdBy = createdBy,
        )
    }
