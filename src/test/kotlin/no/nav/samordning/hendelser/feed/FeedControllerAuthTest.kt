package no.nav.samordning.hendelser.feed

import no.nav.samordning.hendelser.TestAuthHelper.emptyToken
import no.nav.samordning.hendelser.TestAuthHelper.expiredToken
import no.nav.samordning.hendelser.TestAuthHelper.futureToken
import no.nav.samordning.hendelser.TestAuthHelper.token
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Tests authentication/authorization (token validation) of the feed controller API.
 */
@SpringBootTest
@AutoConfigureMockMvc
internal class FeedControllerAuthTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun valid_token_issuer_is_authorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, true))
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun valid_orgno_and_tpnr_is_authorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_2, true))
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isOk)
    }

    @Test
    @Throws(Exception::class)
    fun expired_token_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, expiredToken(ORG_NUMBER_2, "https://badserver/provider/"))
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun future_token_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, futureToken(ORG_NUMBER_2, "https://badserver/provider/"))
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun invalid_token_signature_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, false))
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun invalid_token_issuer_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
            .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, false, "http://localhost:8080"))
            .param(TPNR_PARAM_NAME, TPNR_1))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun invalid_orgno_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token("1111111111", false))
                .param(TPNR_PARAM_NAME, TPNR_1))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun invalid_tpnr_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token(ORG_NUMBER_1, false))
                .param(TPNR_PARAM_NAME, "1235"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun invalid_orgno_and_tpnr_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, token("9999999999", true))
                .param(TPNR_PARAM_NAME, "2000"))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun no_token_is_unauthorized() {
        mockMvc.perform(get(ENDPOINT)
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isUnauthorized)
    }

    @Test
    @Throws(Exception::class)
    fun missing_required_parameter_returns_bad_request() {
        mockMvc.perform(get(ENDPOINT)
                .header(AUTH_HEADER_NAME, emptyToken())
                .param(TPNR_PARAM_NAME, TPNR_2))
                .andExpect(status().isBadRequest)
    }

    companion object {
        private const val ENDPOINT = "/hendelser"
        private const val AUTH_HEADER_NAME = "Authorization"
        private const val TPNR_PARAM_NAME = "tpnr"
        private const val ORG_NUMBER_1 = "0000000000"
        private const val ORG_NUMBER_2 = "4444444444"
        private const val TPNR_1 = "1000"
        private const val TPNR_2 = "4000"
    }
}
