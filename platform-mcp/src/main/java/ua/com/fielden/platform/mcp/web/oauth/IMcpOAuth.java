package ua.com.fielden.platform.mcp.web.oauth;

import com.google.inject.ImplementedBy;

import java.util.List;

/// OAuth functionality for the MCP server.
///
/// ##### References
/// * [The OAuth 2.1 Authorization Framework](https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-13.html)
/// * [RFC 9278 - OAuth 2.0 Protected Resource Metadata](https://datatracker.ietf.org/doc/html/rfc9728)
///
@ImplementedBy(DefaultMcpOAuth.class)
public interface IMcpOAuth {

    /// Path to the Protected Resource Metadata document, as defined by [RFC 9728, 3.](https://datatracker.ietf.org/doc/html/rfc9728#name-obtaining-protected-resourc).
    ///
    String protectedResourceMetadataPath();

    /// Contents of the Protected Resource Metadata document.
    ///
    IProtectedResourceMetadata protectedResourceMetadata();

    interface IProtectedResourceMetadata {

        /// _Resource identifier_ for the MCP resource.
        /// In practice this is the URI of the MCP web resource.
        /// E.g., `https://app.com/mcp`.
        ///
        String resource();

        /// A list of URIs of all supported authorisation servers.
        /// In practice this list contains only the application's URI.
        ///
        List<String> authorisationServers();

    }


}
