package ru.semka.domain.entity

import jakarta.persistence.*
import ru.semka.domain.enums.OperationType

@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "wallet_id", nullable = false)
    var walletId: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tip: OperationType = OperationType.EXPENSE,

    @Column(name = "icon_key", nullable = false)
    var iconKey: String = "cart",

    @Column(name = "color_bg", nullable = false)
    var colorBg: String = "#AAF0D1",

    @Column(name = "created_by")
    var createdBy: Long? = null,
)
