package ua.com.fielden.platform.mcp.web;

import com.google.inject.Inject;
import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.McpConfig;

public class McpWebUiResources implements IMcpWebUiResources {

    private final McpWebResourceGuard.Factory mcpWebResourceGuardFactory;
    private final McpResourceFactory mcpResourceFactory;
    private final McpConfig mcpConfig;

    @Inject
    protected McpWebUiResources(
            final McpWebResourceGuard.Factory mcpWebResourceGuardFactory,
            final McpResourceFactory mcpResourceFactory,
            final McpConfig mcpConfig)
    {
        this.mcpWebResourceGuardFactory = mcpWebResourceGuardFactory;
        this.mcpResourceFactory = mcpResourceFactory;
        this.mcpConfig = mcpConfig;
    }

    public void register(final Router router, final Context context) {
        final var mcpWebResourceGuard = mcpWebResourceGuardFactory.create(context);
        mcpWebResourceGuard.setNext(mcpResourceFactory);
        router.attach(mcpConfig.resourcePath(), mcpWebResourceGuard);
    }

}
