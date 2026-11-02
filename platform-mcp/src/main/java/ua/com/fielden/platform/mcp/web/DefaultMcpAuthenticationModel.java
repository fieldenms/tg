package ua.com.fielden.platform.mcp.web;

import jakarta.inject.Inject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.security.user.User;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.mcp.McpConfig.WEB_API_KEY_MCP_HTTP_HEADER;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

class DefaultMcpAuthenticationModel implements IMcpAuthenticationModel {

    private final McpConfig mcpConfig;
    private final ICompanionObjectFinder coFinder;

    @Inject
    protected DefaultMcpAuthenticationModel(final McpConfig mcpConfig, final ICompanionObjectFinder coFinder) {
        this.mcpConfig = mcpConfig;
        this.coFinder = coFinder;
    }

    @Override
    public Result authenticate(final Request request, final Response response) {
        final var maybeRequestApiKey = Optional.ofNullable(request.getHeaders().getFirstValue(WEB_API_KEY_MCP_HTTP_HEADER, true));
        if (maybeRequestApiKey.filter(mcpConfig.apiKey()::equals).isEmpty()) {
            response.setStatus(CLIENT_ERROR_FORBIDDEN);
            response.setEntity(encodedRepresentation(new ByteArrayInputStream("Access denied.".getBytes(UTF_8)), MediaType.TEXT_PLAIN));
            return failuref("%s is missing or invalid.", WEB_API_KEY_MCP_HTTP_HEADER);
        }
        final var mcpUser = coFinder.find(User.class, true).findByKey(mcpConfig.mcpUser());
        if (mcpUser == null) {
            return failuref("MCP user [%s] is missing.", mcpConfig.mcpUser());
        }
        return successful(mcpUser);
    }

}
