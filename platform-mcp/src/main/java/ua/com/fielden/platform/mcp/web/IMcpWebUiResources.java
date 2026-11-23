package ua.com.fielden.platform.mcp.web;

import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.web.application.AbstractWebUiResources;

/// Web UI resources configuration for the Model Context Protocol (MCP) integration.
///
/// The platform does not register these resources.
/// An application should register them by itself by calling [#register] within [AbstractWebUiResources#registerUnguardedDomainWebResources].
///
public interface IMcpWebUiResources {

    /// Registers the MCP web resource at [McpConfig#MCP_RESOURCE_PATH].
    ///
    /// Access to the resource is guarded by the API key ([McpConfig#WEB_API_KEY_MCP]).
    ///
    /// Each request is processed under an application user as specified by [McpConfig#MCP_USER].
    ///
    void register(Router router, Context context);

}
