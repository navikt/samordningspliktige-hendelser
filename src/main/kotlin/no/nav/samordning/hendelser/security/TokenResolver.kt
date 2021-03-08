package no.nav.samordning.hendelser.security

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.samordning.hendelser.consumer.Consumer
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.CLIENT_CONSUMER_OBJECT
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.ISSUER
import no.nav.samordning.hendelser.security.TokenResolver.ClaimKeys.SCOPE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenError
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes.INVALID_REQUEST
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import java.util.*
import javax.servlet.http.HttpServletRequest

class TokenResolver(
    private val bearerTokenResolver: BearerTokenResolver,
    private val tpRegisteretConsumer: TpregisteretConsumer,
) : BearerTokenResolver {

    private object ClaimKeys {
        const val CLIENT_CONSUMER_OBJECT = "consumer"
        const val ISSUER = "iss"
        const val SCOPE = "scope"
    }

    override fun resolve(request: HttpServletRequest) = bearerTokenResolver
        .resolve(request)?.takeIf {
            validateClaims(
                getClaims(it),
                request.getParameter("tpnr").substringBefore('?')
            )
        }

    private fun validateClaims(claims: JsonNode, tpnr: String) =
        claims.validScope().and(claims.validOrganisation(tpnr)).also {
            log(if (it) "Valid" else "Invalid", tpnr, claims)
            if (!it) LOG.info("Invalid token")
        }

    private fun JsonNode.validOrganisation(tpnr: String) =
        tpRegisteretConsumer.validateOrganisation(
            Consumer.parse(get(CLIENT_CONSUMER_OBJECT).asText()).getOrgno(),
            tpnr
        )!!

    companion object {

        private val LOG = LoggerFactory.getLogger(TokenResolver::class.java)
        private const val REQUIRED_SCOPE = "nav:pensjon/v1/samordning"
        private val mapper = ObjectMapper()

        private val REQUIRED_CLAIM_KEYS = listOf(
            CLIENT_CONSUMER_OBJECT,
            ISSUER,
            SCOPE
        )

        private fun getClaims(token: String) = mapper.readTree(decode(token))
            .also(::checkThatRequiredParametersAreProvided)

        private fun decode(token: String) = String(
            Base64.getUrlDecoder().decode(
                JWT.decode(token).payload
            )
        )

        private fun checkThatRequiredParametersAreProvided(claims: JsonNode) = REQUIRED_CLAIM_KEYS
            .filterNot(claims::has)
            .joinToString(", ")
            .takeUnless(String::isEmpty)
            ?.let { throw tokenError("Missing parameters: $it") }
            ?: LOG.logClaims(claims)

        private fun Logger.logClaims(claims: JsonNode) {
            info("ORNGNR: ${Consumer.parse(claims[CLIENT_CONSUMER_OBJECT].asText()).getOrgno()}")
            info("SCOPE: ${claims[SCOPE].asText()}")
            info("ISSUER: ${claims[ISSUER].asText()}")
        }

        private fun JsonNode.validScope() = this[SCOPE].asText().let { scope ->
            (scope == REQUIRED_SCOPE)
                .also { if (!it) LOG.info("Invalid scope: $scope") }
        }

        private fun tokenError(message: String) = OAuth2AuthenticationException(
            BearerTokenError(
                INVALID_REQUEST, BAD_REQUEST, message,
                "https://tools.ietf.org/html/rfc6750#section-3.1"
            )
        )

        private fun log(status: String, tpnr: String, claims: JsonNode) =
            LOG.info("$status tpnr $tpnr for scope ${claims[SCOPE].asText()}")
    }
}
