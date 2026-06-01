package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.OperationType

/**
 * JPA-сущность таблицы categories.
 * категории привязаны к кошельку: у каждого кошелька свой набор «Продукты», «Зарплата» и т.д.
 * при создании кошелька подставляются шаблоны из CategoryTemplates.
 */
// таблица категорий доходов и расходов
@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // первичный ключ

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0, // id кошелька, которому принадлежит категория

    @Column(nullable = false)
    var name: String = "", // отображаемое название («Продукты»)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tip: OperationType = OperationType.EXPENSE, // INCOME или EXPENSE

    @Column(name = "icon_key", nullable = false)
    var iconKey: String = "cart", // ключ иконки для фронта (cart, home, …)

    @Column(name = "color_bg", nullable = false)
    var colorBg: String = "#AAF0D1", // цвет фона карточки категории в UI

    @Column(name = "created_by")
    var createdBy: Long? = null, // кто создал (user id); null — из системного шаблона
)
