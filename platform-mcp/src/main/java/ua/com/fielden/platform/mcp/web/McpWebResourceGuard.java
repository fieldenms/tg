package ua.com.fielden.platform.mcp.web;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.security.Authenticator;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN;
import static ua.com.fielden.platform.mcp.McpConfig.WEB_API_KEY_MCP_HTTP_HEADER;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

public class McpWebResourceGuard extends Authenticator {

    public interface Factory {
        McpWebResourceGuard create(Context context);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private final McpConfig mcpConfig;
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder coFinder;

    @Inject
    protected McpWebResourceGuard(
            @Assisted final Context context,
            final McpConfig mcpConfig,
            final IUserProvider userProvider,
            final ICompanionObjectFinder coFinder)
    {
        super(context);
        this.mcpConfig = mcpConfig;
        this.userProvider = userProvider;
        this.coFinder = coFinder;
    }

    @Override
    protected boolean authenticate(final Request request, final Response response) {
        final var maybeRequestApiKey = Optional.ofNullable(request.getHeaders().getFirstValue(WEB_API_KEY_MCP_HTTP_HEADER, true));
        if (maybeRequestApiKey.filter(mcpConfig.apiKey()::equals).isEmpty()) {
            LOGGER.warn(() -> "Access denied. %s is missing or invalid.".formatted(WEB_API_KEY_MCP_HTTP_HEADER));
            setForbidden(response);
            return false;
        }

        final var mcpUser = coFinder.find(User.class, true).findByKey(mcpConfig.mcpUser());
        if (mcpUser == null) {
            LOGGER.warn(() -> "Access denied. MCP user [%s] is missing.".formatted(mcpConfig.mcpUser()));
            setForbidden(response);
            return false;
        }
        else {
            userProvider.setUser(mcpUser);
            // Record the current username as the Restlet security User that forms part of the HTTP request information.
            // This information is required for AccessAuditFilter.
            final org.restlet.security.User restletUser = new org.restlet.security.User();
            restletUser.setIdentifier(mcpUser.getId().toString());
            request.getClientInfo().setUser(restletUser);
        }

        return true;
    }

    private static void setForbidden(final Response response) {
        response.setStatus(CLIENT_ERROR_FORBIDDEN);
        response.setEntity(encodedRepresentation(new ByteArrayInputStream("Access denied.".getBytes(UTF_8)), MediaType.TEXT_PLAIN));
    }

}
