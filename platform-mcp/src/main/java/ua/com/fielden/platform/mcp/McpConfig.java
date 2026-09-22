package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.security.user.User;

/// Model Context Protocol (MCP) configuration.
///
/// @param resourcePath
///     MCP resource path, describing the web resource path.
///     For example, if the value is `/mcp`, the MCP resource will be available at `app.com/mcp`.
/// @param apiKey
///     API key for authorising access to the MCP resource.
///     HTTP requests should include the API key using header [#WEB_API_KEY_MCP_HTTP_HEADER].
/// @param mcpUser
///     application user that will be used for all MCP requests.
///     Its value represents the user's key ([User#key]).
///
public record McpConfig (
    String resourcePath,
    String apiKey,
    String mcpUser
) {

    /// Name of the property that specifies [McpConfig#resourcePath].
    ///
    public static final String MCP_RESOURCE_PATH = "mcp.resource.path";

    /// Name of the property that specifies [McpConfig#apiKey].
    ///
    public static final String WEB_API_KEY_MCP = "web.api.key.mcp";
    public static final String WEB_API_KEY_MCP_HTTP_HEADER = "X-API-Key";

    /// Name of the property that specifies [McpConfig#mcpUser].
    ///
    public static final String MCP_USER = "mcp.user";

}
