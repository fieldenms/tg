package ua.com.fielden.platform.mcp.web;

import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.web.application.AbstractWebUiResources;

/// Web UI resources configuration for the Model Context Protocol (MCP) integration.
///
/// The platform does not register these resources.
/// An application should register them by itself by calling [#registerUnguarded] within [AbstractWebUiResources#registerUnguardedDomainWebResources]
/// and [#registerGuarded] within [AbstractWebUiResources#registerDomainWebResources].
///
public interface IMcpWebUiResources {

    /// Registers the MCP web resource at [McpConfig#MCP_RESOURCE_PATH].
    ///
    /// Each request is authenticated by [IMcpAuthenticationModel], and is processed under the authenticated user.
    ///
    void registerUnguarded(Router router, Context context);

    /// Registers guarded MCP web resources.
    ///
    void registerGuarded(Router router, Context context);

}
