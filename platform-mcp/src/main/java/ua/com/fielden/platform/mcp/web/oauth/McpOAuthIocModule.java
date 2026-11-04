package ua.com.fielden.platform.mcp.web.oauth;

import com.google.inject.multibindings.OptionalBinder;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.mcp.McpAccessToken;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.web.IMcpAuthenticationModel;
import ua.com.fielden.platform.mcp.web.IMcpWebUiResources;

import java.util.Properties;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static ua.com.fielden.platform.ioc.IocUtils.PropertySource.*;
import static ua.com.fielden.platform.ioc.IocUtils.findProperty;
import static ua.com.fielden.platform.mcp.McpConfig.MCP_RESOURCE_PATH;

/// An IoC module that adds OAuth-based authentication to the MCP server.
///
/// This module binds [IMcpAuthenticationModel] to an implementation that authenticates requests with OAuth access tokens, sent in header `Authorization` with scheme `Bearer`.
/// MCP clients obtain access tokens through the OAuth 2.1 authorisation code grant, in which the application acts as both the authorisation server and the protected resource.
/// A request to the MCP server is processed under the user who granted the access token, and is subject to that user's authorisation.
///
/// [IMcpAuthenticationModel] is bound as the default of an optional binding, so that it can be replaced with [OptionalBinder#setBinding].
///
/// This module can be installed even if MCP is disabled, in which case it creates no bindings.
///
/// ##### Requirements
///
/// An application that installs this module must:
/// * Register entity types [McpAuthorisationCode] and [McpAccessToken], and create their tables in the database.
/// * Register the MCP clients that may request access in the application properties, as described in [McpOAuthClientRegistry].
///   Requests of clients that are not registered are rejected.
/// * Register every instance of [IMcpWebUiResources], which include the OAuth web resources added by this module.
///
/// ##### Web resources
///
/// The OAuth web resources are described by [IMcpOAuth], and are served at the following paths:
/// * `/.well-known/oauth-protected-resource/<MCP resource path>` -- Protected Resource Metadata, which leads a client to the authorisation server ([McpProtectedResourceMetadataResource]).
/// * `/.well-known/oauth-authorization-server` -- Authorization Server Metadata, which describes the endpoints below ([McpAuthorisationServerMetadataResource]).
/// * [#AUTHORISATION_ENDPOINT_PATH] -- the authorisation endpoint, where the user approves or denies access of a client on a consent page ([McpAuthorisationEndpointResource]).
///   It is guarded, so the user must be logged in to the application.
/// * [#TOKEN_ENDPOINT_PATH] -- the token endpoint, which exchanges an authorisation code for an access token ([McpTokenEndpointResource]).
///
public class McpOAuthIocModule extends AbstractPlatformIocModule {

    public static final String
            AUTHORISATION_ENDPOINT_PATH = "/oauth/authorize",
            TOKEN_ENDPOINT_PATH = "/oauth/token";

    private final Properties props;

    public McpOAuthIocModule(final Properties props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();

        final var maybeMcpResourcePath = findProperty(MCP_RESOURCE_PATH, properties(props), system(), env());
        if (maybeMcpResourcePath.isPresent()) {
            bind(McpOAuthClientRegistry.class).toInstance(McpOAuthClientRegistry.fromProperties(props));
            newOptionalBinder(binder(), IMcpAuthenticationModel.class).setDefault().to(McpOAuthAuthenticationModel.class);
            newSetBinder(binder(), IMcpWebUiResources.class).addBinding().to(McpOAuthWebUiResources.class);
        }
    }

}
