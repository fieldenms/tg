package ua.com.fielden.platform.mcp.web.oauth;

import com.google.inject.ImplementedBy;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.error.Result;

import java.util.List;

/// OAuth functionality for the MCP server.
///
/// ##### References
/// * [The OAuth 2.1 Authorization Framework](https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-13.html)
/// * [RFC 9278 - OAuth 2.0 Protected Resource Metadata](https://datatracker.ietf.org/doc/html/rfc9728)
/// * [RFC 8414 - OAuth 2.0 Authorization Server Metadata](https://datatracker.ietf.org/doc/html/rfc8414)
///
@ImplementedBy(DefaultMcpOAuth.class)
public interface IMcpOAuth {

    /// Path to the Protected Resource Metadata document, as defined by [RFC 9728, 3.](https://datatracker.ietf.org/doc/html/rfc9728#name-obtaining-protected-resourc).
    ///
    String protectedResourceMetadataPath();

    /// Contents of the Protected Resource Metadata document.
    ///
    IProtectedResourceMetadata protectedResourceMetadata();

    /// Path to the Authorization Server Metadata document, as defined by [RFC 8414, 3.](https://datatracker.ietf.org/doc/html/rfc8414#section-3).
    ///
    String authorisationServerMetadataPath();

    /// Contents of the Authorization Server Metadata document.
    ///
    IAuthorisationServerMetadata authorisationServerMetadata();

    Result validateClient(@Nullable String clientId);

    Result validateClient(@Nullable String clientId, @Nullable String redirectUri);

    interface IAuthorisationServerMetadata {

        /// _Issuer identifier_ of the authorisation server.
        /// In practice this is the application's URI.
        /// E.g., `https://app.com`.
        ///
        String issuer();

        /// URL of the authorisation endpoint.
        /// E.g., `https://app.com/oauth/authorize`.
        ///
        String authorisationEndpoint();

        /// URL of the token endpoint.
        /// E.g., `https://app.com/oauth/token`.
        ///
        String tokenEndpoint();

        /// A list of supported `response_type` values.
        /// The authorisation code flow is the only supported flow, hence `["code"]`.
        ///
        List<String> responseTypesSupported();

        /// A list of supported PKCE code challenge methods, which includes `"S256"`.
        ///
        List<String> codeChallengeMethodsSupported();

        /// A list of supported grant types.
        /// The authorisation code flow is the only supported flow, hence `["authorization_code"]`.
        ///
        List<String> grantTypesSupported();

        /// A list of client authentication methods supported by the token endpoint.
        /// `"none"` covers public clients.
        ///
        List<String> tokenEndpointAuthMethodsSupported();

        /// Whether the authorisation server provides the `iss` parameter in the authorisation response, as defined by [RFC 9207](https://datatracker.ietf.org/doc/html/rfc9207).
        ///
        boolean authorisationResponseIssParameterSupported();

    }

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
