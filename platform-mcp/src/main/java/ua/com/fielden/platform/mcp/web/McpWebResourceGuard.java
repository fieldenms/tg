package ua.com.fielden.platform.mcp.web;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Authenticator;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

public class McpWebResourceGuard extends Authenticator {

    public interface Factory {
        McpWebResourceGuard create(Context context);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private final IUserProvider userProvider;
    private final IMcpAuthenticationModel authModel;

    @Inject
    protected McpWebResourceGuard(
            @Assisted final Context context,
            final IUserProvider userProvider,
            final IMcpAuthenticationModel authModel)
    {
        super(context);
        this.userProvider = userProvider;
        this.authModel = authModel;
    }

    @Override
    protected boolean authenticate(final Request request, final Response response) {
        final var authResult = authModel.authenticate(request, response);
        if (!authResult.isSuccessful()) {
            LOGGER.warn(() -> "Access denied. MCP authentication failed. %s".formatted(authResult.getMessage()));
            return false;
        }
        final var authUser = (User) authResult.getInstance();

        userProvider.setUser(authUser);

        // Record the current username as the Restlet security User that forms part of the HTTP request information.
        // This information is required for AccessAuditFilter.
        final org.restlet.security.User restletUser = new org.restlet.security.User();
        restletUser.setIdentifier(authUser.getId().toString());
        request.getClientInfo().setUser(restletUser);

        return true;
    }

}
