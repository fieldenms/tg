package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAccessTokenCo;
import ua.com.fielden.platform.mcp.web.IMcpAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.regex.Pattern;

import static org.restlet.data.ChallengeScheme.HTTP_OAUTH_BEARER;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.CLIENT_ERROR_UNAUTHORIZED;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.MiscUtilities.uriPath;

/// OAuth-based authentication for the MCP server.
///
/// A request is authenticated with an access token, which is sent in the `Authorization` header with the `Bearer` scheme.
/// The checks are performed in the order they appear in [#authenticate], stopping at the first failure.
///
/// Failures are reported in the `WWW-Authenticate` header, as described in [RFC 6750, 3.](https://datatracker.ietf.org/doc/html/rfc6750#section-3).
/// The header always carries `resource_metadata`, so that the client can start discovery ([RFC 9728, 5.1.](https://datatracker.ietf.org/doc/html/rfc9728#name-www-authenticate-response)).
///
/// @see IMcpOAuth
///
class McpOAuthAuthenticationModel implements IMcpAuthenticationModel {

    /// A bearer token, as defined by [RFC 6750, 2.1.](https://datatracker.ietf.org/doc/html/rfc6750#section-2.1).
    ///
    private static final Pattern BEARER_TOKEN = Pattern.compile("[A-Za-z0-9\\-._~+/]+=*");

    private final IMcpOAuth mcpOAuth;
    private final String appUri;
    private final ICompanionObjectFinder coFinder;

    @Inject
    protected McpOAuthAuthenticationModel(
            final IMcpOAuth mcpOAuth,
            final @AppUri String appUri,
            final ICompanionObjectFinder coFinder)
    {
        this.mcpOAuth = mcpOAuth;
        this.appUri = appUri;
        this.coFinder = coFinder;
    }

    @Override
    public Result authenticate(final Request request, final Response response) {
        final var authorisationHeaders = request.getHeaders().getValuesArray(HeaderConstants.HEADER_AUTHORIZATION, true);
        if (authorisationHeaders.length == 0) {
            return reject(response, CLIENT_ERROR_UNAUTHORIZED, null, "Missing authentication.");
        }
        if (authorisationHeaders.length > 1) {
            return reject(response, CLIENT_ERROR_BAD_REQUEST, "invalid_request", "Multiple Authorization headers.");
        }
        final var credentials = authorisationHeaders[0].strip().split("\\s+", 2);
        final var authScheme = credentials[0];
        if (!HTTP_OAUTH_BEARER.getTechnicalName().equalsIgnoreCase(authScheme)) {
            return reject(response, CLIENT_ERROR_UNAUTHORIZED, null, "Unsupported authentication scheme [%s].".formatted(authScheme));
        }
        final String token;
        if (credentials.length < 2 || !BEARER_TOKEN.matcher(token = credentials[1]).matches()) {
            return reject(response, CLIENT_ERROR_BAD_REQUEST, "invalid_request", "Malformed bearer token.");
        }

        final McpAccessTokenCo coAccessToken = coFinder.find(McpAccessToken.class, true);
        final var maybeAccessToken = coAccessToken.findByTokenValue(token);
        if (maybeAccessToken.isEmpty()) {
            return reject(response, CLIENT_ERROR_UNAUTHORIZED, "invalid_token", "Unknown access token.");
        }
        final var accessToken = maybeAccessToken.get();

        if (coAccessToken.isExpired(accessToken)) {
            return reject(response, CLIENT_ERROR_UNAUTHORIZED, "invalid_token", "The access token expired.");
        }

        // TODO Check that neither the access token, nor the client's grant, nor the client registration was revoked.
        //      If any was, error code invalid_token.

        // TODO Check that the resource of the access token equals the resource of this MCP server.
        //      If not, error code invalid_token.

        final IUser coUser = coFinder.find(User.class, true);
        final var user = coUser.findUser(accessToken.getAuthorisationCode().getUser().getKey());
        // TODO Check that the user is allowed to use MCP.
        if (!user.isActive()) {
            // TODO Revoke the access token.
            return reject(response, CLIENT_ERROR_UNAUTHORIZED, "invalid_token", "The user of the access token is inactive.");
        }

        return successful(user);
    }

    /// Rejects the request, reporting the failure in the `WWW-Authenticate` header, as described in [RFC 6750, 3.](https://datatracker.ietf.org/doc/html/rfc6750#section-3).
    ///
    /// ```
    /// WWW-Authenticate: Bearer resource_metadata="<url>", error="<code>", error_description="<text>"
    /// ```
    ///
    /// @param errorCode  the error code, or `null` if the request lacks authentication information,
    ///                   in which case neither `error` nor `error_description` are present
    /// @param errorDesc  a description of the failure, which is also the message of the returned failure
    ///
    private Result reject(final Response response, final Status status, final @Nullable String errorCode, final String errorDesc) {
        response.setStatus(status);
        final var resourceMetadata = "resource_metadata=\"%s\"".formatted(uriPath(appUri, mcpOAuth.protectedResourceMetadataPath()));
        final var challenge = new ChallengeRequest(HTTP_OAUTH_BEARER);
        challenge.setRawValue(errorCode == null
                              ? resourceMetadata
                              : "%s, error=\"%s\", error_description=\"%s\"".formatted(resourceMetadata, errorCode, errorDesc));
        response.getChallengeRequests().add(challenge);
        return failure(errorDesc);
    }

}
