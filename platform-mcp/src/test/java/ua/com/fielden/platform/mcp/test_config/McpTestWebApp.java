package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.Inject;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.web.McpResourceFactory;

/// This is a web application specific to testing of the MCP server.
///
public class McpTestWebApp extends Application {

    private final McpResourceFactory mcpResourceFactory;

    @Inject
    protected McpTestWebApp(final McpResourceFactory mcpResourceFactory) {
        this.mcpResourceFactory = mcpResourceFactory;
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());

        router.attach(TgMcpServerTestCaseConfiguration.PATH_MCP, mcpResourceFactory);

        return router;
    }

}
