package ua.com.fielden.platform.mcp;

/// Model Context Protocol (MCP) configuration.
///
/// @param resourcePath
///     MCP resource path, describing the web resource path.
///     For example, if the value is `/mcp`, the MCP resource will be available at `app.com/mcp`.
///
public record McpConfig (
    String resourcePath
) {

    /// Name of the property that specifies [McpConfig#resourcePath].
    ///
    public static final String MCP_RESOURCE_PATH = "mcp.resource.path";

}
