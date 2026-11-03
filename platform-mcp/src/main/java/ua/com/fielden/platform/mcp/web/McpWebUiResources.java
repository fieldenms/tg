package ua.com.fielden.platform.mcp.web;

import com.google.inject.Inject;
import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.mcp.web.oauth.IMcpOAuth;
import ua.com.fielden.platform.mcp.web.oauth.McpAuthorisationEndpointResource;
import ua.com.fielden.platform.mcp.web.oauth.McpAuthorisationServerMetadataResource;
import ua.com.fielden.platform.mcp.web.oauth.McpProtectedResourceMetadataResource;

import static ua.com.fielden.platform.utils.MiscUtilities.mkUri;

public class McpWebUiResources implements IMcpWebUiResources {

    private final McpWebResourceGuard.Factory mcpWebResourceGuardFactory;
    private final McpResourceFactory mcpResourceFactory;
    private final McpConfig mcpConfig;
    private final IMcpOAuth mcpOAuth;
    private final McpProtectedResourceMetadataResource.Factory mcpProtectedResourceMetadataResourceFactory;
    private final McpAuthorisationServerMetadataResource.Factory mcpAuthorisationServerMetadataResourceFactory;
    private final McpAuthorisationEndpointResource.Factory mcpAuthorisationEndpointResourceFactory;

    @Inject
    protected McpWebUiResources(
            final McpWebResourceGuard.Factory mcpWebResourceGuardFactory,
            final McpResourceFactory mcpResourceFactory,
            final McpConfig mcpConfig,
            final IMcpOAuth mcpOAuth,
            final McpProtectedResourceMetadataResource.Factory mcpProtectedResourceMetadataResourceFactory,
            final McpAuthorisationServerMetadataResource.Factory mcpAuthorisationServerMetadataResourceFactory,
            final McpAuthorisationEndpointResource.Factory mcpAuthorisationEndpointResourceFactory)
    {
        this.mcpWebResourceGuardFactory = mcpWebResourceGuardFactory;
        this.mcpProtectedResourceMetadataResourceFactory = mcpProtectedResourceMetadataResourceFactory;
        this.mcpResourceFactory = mcpResourceFactory;
        this.mcpConfig = mcpConfig;
        this.mcpOAuth = mcpOAuth;
        this.mcpAuthorisationServerMetadataResourceFactory = mcpAuthorisationServerMetadataResourceFactory;
        this.mcpAuthorisationEndpointResourceFactory = mcpAuthorisationEndpointResourceFactory;
    }

    public void registerUnguarded(final Router router, final Context context) {
        final var mcpWebResourceGuard = mcpWebResourceGuardFactory.create(context);
        mcpWebResourceGuard.setNext(mcpResourceFactory);
        router.attach(mcpConfig.resourcePath(), mcpWebResourceGuard);

        router.attach(mcpOAuth.protectedResourceMetadataPath(), mcpProtectedResourceMetadataResourceFactory);
        router.attach(mcpOAuth.authorisationServerMetadataPath(), mcpAuthorisationServerMetadataResourceFactory);
    }

    @Override
    public void registerGuarded(final Router router, final Context context) {
        // Assuming the application server is also the authorisation server.
        router.attach(mkUri(mcpOAuth.authorisationServerMetadata().authorisationEndpoint()).getRawPath(), mcpAuthorisationEndpointResourceFactory);
    }

}
