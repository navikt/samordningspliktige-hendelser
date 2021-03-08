package no.nav.samordning.hendelser.security

import no.nav.samordning.hendelser.TestTokenHelper.serviceToken
import no.nav.samordning.hendelser.TestTokenHelper.token
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import java.security.NoSuchAlgorithmException
import javax.servlet.http.HttpServletRequest

/**
 * Tests TokenResolver in isolation.
 */
internal class TokenResolverTest {
    private lateinit var tpRegisteretConsumer: TpregisteretConsumer
    private lateinit var bearerTokenResolver: BearerTokenResolver
    private lateinit var request: HttpServletRequest

    @BeforeEach
    fun setUp() {
        request = mock(HttpServletRequest::class.java)
        bearerTokenResolver = mock(BearerTokenResolver::class.java)
        tpRegisteretConsumer = mock(TpregisteretConsumer::class.java)
        `when`(request.getParameter("tpnr")).thenReturn("123?antall=1")
        `when`<Boolean>(tpRegisteretConsumer.validateOrganisation(GOOD_ORG_NUMBER, "123")).thenReturn(true)
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun resolving_good_token_shall_return_token() {
        `when`(bearerTokenResolver.resolve(any())).thenReturn(token(GOOD_ORG_NUMBER, true))
        val resolver = TokenResolver(bearerTokenResolver, tpRegisteretConsumer)

        val result = resolver.resolve(request)

        assertEquals("ey", result!!.substring(0, 2))
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun resolving_invalid_scope_token_shall_return_null() {
        `when`(bearerTokenResolver.resolve(any())).thenReturn(token("BAD SCOPE", GOOD_ORG_NUMBER, true))
        val resolver = TokenResolver(bearerTokenResolver, tpRegisteretConsumer)

        val result = resolver.resolve(request)

        assertNull(result)
    }

    @Test
    @Throws(NoSuchAlgorithmException::class)
    fun resolving_invalid_org_token_shall_return_null() {
        `when`(bearerTokenResolver.resolve(any())).thenReturn(token("-1", true))
        val resolver = TokenResolver(bearerTokenResolver, tpRegisteretConsumer)

        val result = resolver.resolve(request)

        assertNull(result)
    }

    @Test
    fun resolving_token_with_missing_claims_shall_give_exception_telling_which_claims() {
        `when`(bearerTokenResolver.resolve(any())).thenReturn(serviceToken())
        val resolver = TokenResolver(bearerTokenResolver, tpRegisteretConsumer)

        val exception = assertThrows(OAuth2AuthenticationException::class.java) { resolver.resolve(request) }

        assertEquals("Missing parameters: consumer, scope", exception.message)
    }

    companion object {
        private const val GOOD_ORG_NUMBER = "987654321"
    }
}