package ru.semka.service

import ru.semka.domain.entity.*
import ru.semka.dto.*
import java.math.BigDecimal
import java.math.RoundingMode

fun UserEntity.toDto() = UserDto(id!!, email, nick, role)

fun CategoryEntity.toDto() = CategoryDto(id!!, walletId, name, tip, iconKey, colorBg)

fun TransactionEntity.toDto() = TransactionDto(
    id = id!!,
    walletId = walletId,
    authorId = authorId,
    authorNick = author?.nick ?: "?",
    categoryId = categoryId,
    categoryName = category?.name ?: "?",
    type = type,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment,
    splitDoli = splits.takeIf { it.isNotEmpty() }?.map {
        SplitShareDto(it.user?.nick ?: "?", it.shareAmount)
    },
)

fun WalletMemberEntity.toDto(nick: String) = MemberDto(id!!, userId, nick, memberRole, canSeeBudget)

fun RecurringRuleEntity.toDto() = RecurringRuleDto(
    id = id!!,
    walletId = walletId,
    categoryId = categoryId,
    categoryName = category?.name,
    amount = amount,
    dayOfMonth = dayOfMonth,
    nextRunDate = nextRunDate,
    active = active,
    comment = comment,
)

fun BigDecimal.money(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
