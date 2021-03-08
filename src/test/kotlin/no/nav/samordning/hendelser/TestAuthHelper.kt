package no.nav.samordning.hendelser

import no.nav.samordning.hendelser.TestTokenHelper.createExpiredJwt
import no.nav.samordning.hendelser.TestTokenHelper.createFutureJwt
import java.security.NoSuchAlgorithmException

object TestAuthHelper {

    private const val AUTH_SCHEME = "Bearer"

    @Throws(NoSuchAlgorithmException::class)
    fun token(orgno: String, verifiedSignature: Boolean, iss: String? = "https://badserver/provider/") =
        header(TestTokenHelper.token(orgno, verifiedSignature, iss))

    fun expiredToken(orgno: String, iss: String?) = header(createExpiredJwt(orgno, iss))

    fun futureToken(orgno: String, iss: String?) = header(createFutureJwt(orgno, iss))

    fun serviceToken() = header(TestTokenHelper.serviceToken())

    fun emptyToken() = header(TestTokenHelper.emptyToken())

    private fun header(token: String) = "$AUTH_SCHEME $token"
}
