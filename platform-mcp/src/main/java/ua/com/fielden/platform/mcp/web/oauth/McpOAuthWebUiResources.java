package ua.com.fielden.platform.mcp.web.oauth;

import com.google.inject.Inject;
import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.web.IMcpWebUiResources;

import static ua.com.fielden.platform.utils.MiscUtilities.mkUri;

class McpOAuthWebUiResources implements IMcpWebUiResources {

    private final IMcpOAuth mcpOAuth;
    private final McpProtectedResourceMetadataResource.Factory mcpProtectedResourceMetadataResourceFactory;
    private final McpAuthorisationServerMetadataResource.Factory mcpAuthorisationServerMetadataResourceFactory;
    private final McpAuthorisationEndpointResource.Factory mcpAuthorisationEndpointResourceFactory;
    private final McpTokenEndpointResource.Factory mcpTokenEndpointResourceFactory;

    @Inject
    protected McpOAuthWebUiResources(
            final IMcpOAuth mcpOAuth,
            final McpProtectedResourceMetadataResource.Factory mcpProtectedResourceMetadataResourceFactory,
            final McpAuthorisationServerMetadataResource.Factory mcpAuthorisationServerMetadataResourceFactory,
            final McpAuthorisationEndpointResource.Factory mcpAuthorisationEndpointResourceFactory,
            final McpTokenEndpointResource.Factory mcpTokenEndpointResourceFactory)
    {
        this.mcpProtectedResourceMetadataResourceFactory = mcpProtectedResourceMetadataResourceFactory;
        this.mcpOAuth = mcpOAuth;
        this.mcpAuthorisationServerMetadataResourceFactory = mcpAuthorisationServerMetadataResourceFactory;
        this.mcpAuthorisationEndpointResourceFactory = mcpAuthorisationEndpointResourceFactory;
        this.mcpTokenEndpointResourceFactory = mcpTokenEndpointResourceFactory;
    }

    @Override
    public void registerUnguarded(final Router router, final Context context) {
        router.attach(mcpOAuth.protectedResourceMetadataPath(), mcpProtectedResourceMetadataResourceFactory);
        router.attach(mcpOAuth.authorisationServerMetadataPath(), mcpAuthorisationServerMetadataResourceFactory);
        // Assuming the application server is also the authorisation server.
        router.attach(mkUri(mcpOAuth.authorisationServerMetadata().tokenEndpoint()).getRawPath(), mcpTokenEndpointResourceFactory);
    }

    @Override
    public void registerGuarded(final Router router, final Context context) {
        // Assuming the application server is also the authorisation server.
        router.attach(mkUri(mcpOAuth.authorisationServerMetadata().authorisationEndpoint()).getRawPath(), mcpAuthorisationEndpointResourceFactory);
    }

}
