package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.inject.Inject;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.nio.file.Path;
import java.util.List;

public class DefaultMcpOAuth implements IMcpOAuth {

    private final String appUri;
    private final McpConfig mcpConfig;

    @Inject
    protected DefaultMcpOAuth(final @AppUri String appUri, final McpConfig mcpConfig) {
        this.appUri = appUri;
        this.mcpConfig = mcpConfig;
    }

    @Override
    public String protectedResourceMetadataPath() {
        return Path.of("/.well-known/oauth-protected-resource", mcpConfig.resourcePath()).toString();
    }

    @Override
    public IProtectedResourceMetadata protectedResourceMetadata() {
        return new IProtectedResourceMetadata() {
            @Override
            public String resource() {
                return Path.of(appUri, mcpConfig.resourcePath()).toString();
            }

            @Override
            public List<String> authorisationServers() {
                return List.of(appUri);
            }
        };
    }

}
