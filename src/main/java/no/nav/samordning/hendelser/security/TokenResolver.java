package no.nav.samordning.hendelser.security;

import com.auth0.jwt.JWT;
import no.nav.samordning.hendelser.consumer.TpregisteretConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class TokenResolver implements org.springframework.security.oauth2.server.resource.web.BearerTokenResolver {

    @Autowired
    private TpregisteretConsumer tpregisteretConsumer;

    @Override
    public String resolve(HttpServletRequest request) {
        var token = new DefaultBearerTokenResolver().resolve(request);
        var claims = getClaims(token);
        var tpnr = request.getParameter("tpnr");

        if (tpregisteretConsumer.validateOrganisation(claims.get("client_orgno"), tpnr)) {
            return token;
        }

        return null;
    }

    private Map<String, String> getClaims(String token) {
        var decoded = JWT.decode(token);
        var payload = new String(Base64.getUrlDecoder().decode(decoded.getPayload()), StandardCharsets.UTF_8);
        var json = new JSONObject(payload);

        try {
            return Map.of("iss", json.getString("iss"),
                    "scope", json.getString("scope"),
                    "client_id", json.getString("client_id"),
                    "client_orgno", json.getString("client_orgno"));
        } catch (JSONException e) {
            throw tokenError("Missing required parameter: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private OAuth2AuthenticationException tokenError(String message, HttpStatus status) {
        var error = new BearerTokenError(BearerTokenErrorCodes.INVALID_REQUEST, status, message,
            "https://tools.ietf.org/html/rfc6750#section-3.1");
        return new OAuth2AuthenticationException(error);
    }
}