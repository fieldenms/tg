package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.inject.Inject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.Status;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.web.IMcpAuthenticationModel;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.nio.file.Path;

import static org.restlet.data.ChallengeScheme.HTTP_OAUTH_BEARER;
import static ua.com.fielden.platform.error.Result.failure;

/// OAuth-based authentication for the MCP server.
///
/// @see IMcpOAuth
///
class McpOAuthAuthenticationModel implements IMcpAuthenticationModel {

    private final IMcpOAuth mcpOAuth;
    private final String appUri;

    @Inject
    protected McpOAuthAuthenticationModel(final IMcpOAuth mcpOAuth, final @AppUri String appUri) {
        this.mcpOAuth = mcpOAuth;
        this.appUri = appUri;
    }

    @Override
    public Result authenticate(final Request request, final Response response) {
        // TODO Token validation (steps 22–24)
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        final var challenge = new ChallengeRequest(HTTP_OAUTH_BEARER);
        challenge.setRawValue("resource_metadata=\"%s\"".formatted(Path.of(appUri, mcpOAuth.protectedResourceMetadataPath())));
        response.getChallengeRequests().add(challenge);
        return failure("Access denied.");
    }

}
