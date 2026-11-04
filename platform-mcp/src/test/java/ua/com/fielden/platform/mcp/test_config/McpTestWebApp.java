package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.Inject;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.web.IMcpWebUiResources;

import java.util.Set;

/// This is a web application specific to testing of the MCP server.
///
/// The MCP web resources are registered in the same way as an application registers them, through every instance of [IMcpWebUiResources].
/// The MCP resource is put behind a guard, which authenticates requests by means of [TestMcpAuthenticationModel].
///
public class McpTestWebApp extends Application {

    private final Set<IMcpWebUiResources> mcpWebUiResources;

    @Inject
    protected McpTestWebApp(final Set<IMcpWebUiResources> mcpWebUiResources) {
        this.mcpWebUiResources = mcpWebUiResources;
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        mcpWebUiResources.forEach(it -> it.registerUnguarded(router, getContext()));
        mcpWebUiResources.forEach(it -> it.registerGuarded(router, getContext()));

        return router;
    }

}
