package ua.com.fielden.platform.mcp.web;

import com.google.inject.Inject;
import org.restlet.Context;
import org.restlet.routing.Router;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.mcp.web.oauth.IMcpOAuth;
import ua.com.fielden.platform.mcp.web.oauth.McpAuthorisationServerMetadataResource;
import ua.com.fielden.platform.mcp.web.oauth.McpProtectedResourceMetadataResource;

public class McpWebUiResources implements IMcpWebUiResources {

    private final McpWebResourceGuard.Factory mcpWebResourceGuardFactory;
    private final McpResourceFactory mcpResourceFactory;
    private final McpConfig mcpConfig;
    private final IMcpOAuth mcpOAuth;
    private final McpProtectedResourceMetadataResource.Factory mcpProtectedResourceMetadataResourceFactory;
    private final McpAuthorisationServerMetadataResource.Factory mcpAuthorisationServerMetadataResourceFactory;

    @Inject
    protected McpWebUiResources(
            final McpWebResourceGuard.Factory mcpWebResourceGuardFactory,
            final McpResourceFactory mcpResourceFactory,
            final McpConfig mcpConfig,
            final IMcpOAuth mcpOAuth,
            final McpProtectedResourceMetadataResource.Factory mcpProtectedResourceMetadataResourceFactory,
            final McpAuthorisationServerMetadataResource.Factory mcpAuthorisationServerMetadataResourceFactory)
    {
        this.mcpWebResourceGuardFactory = mcpWebResourceGuardFactory;
        this.mcpProtectedResourceMetadataResourceFactory = mcpProtectedResourceMetadataResourceFactory;
        this.mcpResourceFactory = mcpResourceFactory;
        this.mcpConfig = mcpConfig;
        this.mcpOAuth = mcpOAuth;
        this.mcpAuthorisationServerMetadataResourceFactory = mcpAuthorisationServerMetadataResourceFactory;
    }

    public void register(final Router router, final Context context) {
        final var mcpWebResourceGuard = mcpWebResourceGuardFactory.create(context);
        mcpWebResourceGuard.setNext(mcpResourceFactory);
        router.attach(mcpConfig.resourcePath(), mcpWebResourceGuard);

        router.attach(mcpOAuth.protectedResourceMetadataPath(), mcpProtectedResourceMetadataResourceFactory);
        router.attach(mcpOAuth.authorisationServerMetadataPath(), mcpAuthorisationServerMetadataResourceFactory);
    }

}
