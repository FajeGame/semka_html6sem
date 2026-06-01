package ru.semka.domain.enums

/**
 * роль участника в одном кошельке (таблица wallet_members.member_role).
 * у одного пользователя в разных кошельках роли могут отличаться.
 */
enum class MemberRole {
    WALLET_OWNER, // владелец: приглашения, бюджет, удаление кошелька, правки чужих операций
    WALLET_MEMBER, // участник: свои операции, выход из кошелька; бюджет — только если canSeeBudget
}
