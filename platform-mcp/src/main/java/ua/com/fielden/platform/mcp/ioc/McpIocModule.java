package ua.com.fielden.platform.mcp.ioc;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Inject;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.mcp.TgMcpServer;
import ua.com.fielden.platform.mcp.web.McpResource;

/// An IoC module that provides the MCP server.
///
/// This module starts the MCP server on application startup.
///
public class McpIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        requestStaticInjection(McpIocModule.class);

        install(new FactoryModuleBuilder().build(McpResource.Factory.class));
    }

    /// The server is started in the constructor of [TgMcpServer].
    ///
    @Inject
    static void startMcpServer(final TgMcpServer server) {}

}
