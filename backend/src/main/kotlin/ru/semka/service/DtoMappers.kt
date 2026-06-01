package ru.semka.service

import ru.semka.domain.entity.*
import ru.semka.dto.*
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * extension-функции: Entity → DTO для ответов API.
 * вызываются из сервисов перед return; не содержат бизнес-правил.
 */

// UserEntity → UserDto
fun UserEntity.toDto() = UserDto(id!!, email, nick, role)

// CategoryEntity → CategoryDto
fun CategoryEntity.toDto() = CategoryDto(id!!, walletId, name, tip, iconKey, colorBg)

// TransactionEntity → TransactionDto (нужны загруженные category, author, splits)
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

// WalletMemberEntity + nick → MemberDto
fun WalletMemberEntity.toDto(nick: String) = MemberDto(id!!, userId, nick, memberRole, canSeeBudget)

// RecurringRuleEntity → RecurringRuleDto
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

// округление денег до 2 знаков
fun BigDecimal.money(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
