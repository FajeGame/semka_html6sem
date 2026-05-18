package ru.semka

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import ru.semka.domain.entity.WalletMemberEntity
import ru.semka.domain.enums.MemberRole
import ru.semka.exception.ApiException
import ru.semka.repository.WalletMemberRepository
import ru.semka.security.AppUserDetails
import ru.semka.service.WalletAccessService
import ru.semka.domain.enums.AppRole
import java.util.Optional

class WalletAccessServiceTest {

    private val memberRepository = Mockito.mock(WalletMemberRepository::class.java)
    private val access = WalletAccessService(memberRepository)

    private val user = AppUserDetails(1L, "a@t.ru", "papa", AppRole.USER, "hash")

    @Test
    fun requireOwner_ok() {
        val member = WalletMemberEntity(
            walletId = 1,
            userId = 1,
            memberRole = MemberRole.WALLET_OWNER,
            canSeeBudget = true,
        )
        Mockito.`when`(memberRepository.findByWalletIdAndUserId(1, 1)).thenReturn(Optional.of(member))
        val m = access.requireOwner(1, user)
        assertEquals(MemberRole.WALLET_OWNER, m.memberRole)
    }

    @Test
    fun requireOwner_forbiddenForMember() {
        val member = WalletMemberEntity(
            walletId = 1,
            userId = 1,
            memberRole = MemberRole.WALLET_MEMBER,
            canSeeBudget = false,
        )
        Mockito.`when`(memberRepository.findByWalletIdAndUserId(1, 1)).thenReturn(Optional.of(member))
        assertThrows(ApiException::class.java) { access.requireOwner(1, user) }
    }
}
