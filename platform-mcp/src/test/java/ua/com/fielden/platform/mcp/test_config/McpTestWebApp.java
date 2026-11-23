package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.Inject;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.web.IMcpWebUiResources;

/// This is a web application specific to testing of the MCP server.
///
/// The MCP resource is registered in the same way as an application registers it, through [IMcpWebUiResources],
/// which puts the MCP web resource behind a guard.
/// Therefore, requests made by tests must carry a valid API key.
///
public class McpTestWebApp extends Application {

    private final IMcpWebUiResources mcpWebUiResources;

    @Inject
    protected McpTestWebApp(final IMcpWebUiResources mcpWebUiResources) {
        this.mcpWebUiResources = mcpWebUiResources;
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        mcpWebUiResources.register(router, getContext());

        return router;
    }

}
