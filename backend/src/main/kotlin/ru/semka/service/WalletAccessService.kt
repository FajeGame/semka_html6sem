package ru.semka.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import ru.semka.domain.entity.WalletMemberEntity
import ru.semka.domain.enums.MemberRole
import ru.semka.exception.ApiException
import ru.semka.repository.WalletMemberRepository
import ru.semka.security.AppUserDetails

@Service
class WalletAccessService(
    private val memberRepository: WalletMemberRepository,
) {
    fun requireMember(walletId: Long, user: AppUserDetails): WalletMemberEntity =
        memberRepository.findByWalletIdAndUserId(walletId, user.id)
            .orElseThrow { ApiException("FORBIDDEN", "нет доступа к кошельку", HttpStatus.FORBIDDEN) }

    fun requireOwner(walletId: Long, user: AppUserDetails): WalletMemberEntity {
        val m = requireMember(walletId, user)
        if (m.memberRole != MemberRole.WALLET_OWNER) {
            throw ApiException("FORBIDDEN", "только владелец", HttpStatus.FORBIDDEN)
        }
        return m
    }

    fun isOwner(walletId: Long, user: AppUserDetails): Boolean =
        memberRepository.findByWalletIdAndUserId(walletId, user.id)
            .map { it.memberRole == MemberRole.WALLET_OWNER }
            .orElse(false)
}
