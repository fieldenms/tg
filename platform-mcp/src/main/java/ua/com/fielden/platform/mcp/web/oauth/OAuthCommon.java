package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.utils.CharSequenceEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Singleton
class OAuthCommon {

    @Inject
    protected OAuthCommon() {}

    /// Computes a code challenge from `codeVerifier` using `method`.
    ///
    public String codeChallenge(final CodeChallengeMethod method, final String codeVerifier) {
        return switch (method) {
            case PLAIN -> codeVerifier;
            case S256 -> {
                final MessageDigest md;
                try {
                    md = MessageDigest.getInstance("SHA-256");
                } catch (final NoSuchAlgorithmException e) {
                    throw new InvalidStateException("SHA-256 not available.", e);
                }
                yield Base64.getUrlEncoder().withoutPadding().encodeToString(md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
            }
        };
    }

    /// [RFC 7636: Proof Key for Code Exchange by OAuth Public Clients](https://www.rfc-editor.org/info/rfc7636/#section-4.2)
    ///
    public enum CodeChallengeMethod {
        /// ```
        /// code_challenge = code_verifier
        /// ```
        ///
        PLAIN,

        /// ```
        /// code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
        /// ```
        ///
        S256;
    }

    public enum AccessTokenModel implements CharSequenceEnum {
        access_token,
        token_type,
        expires_in,
        scope,
        refresh_token
    }

    /// Error codes of the [authorisation endpoint](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-13#section-4.1.2.1)
    /// and the [token endpoint](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-13#section-3.2.4),
    /// including `invalid_target` of [resource indicators](https://datatracker.ietf.org/doc/html/rfc8707#section-2).
    /// Each member is named exactly as the code it represents.
    ///
    public enum ErrorCode implements CharSequenceEnum {
        invalid_request,
        invalid_client,
        invalid_grant,
        invalid_target,
        unsupported_grant_type,
        unsupported_response_type,
        access_denied,
        server_error
    }

}
