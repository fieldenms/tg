package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.nio.file.Path;
import java.util.List;

public class DefaultMcpOAuth implements IMcpOAuth {

    private final String appUri;
    private final McpConfig mcpConfig;

    @Inject
    protected DefaultMcpOAuth(final @AppUri String appUri, final McpConfig mcpConfig) {
        this.appUri = StringUtils.stripEnd(appUri, "/");
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

    @Override
    public String authorisationServerMetadataPath() {
        return "/.well-known/oauth-authorization-server";
    }

    @Override
    public IAuthorisationServerMetadata authorisationServerMetadata() {
        return new IAuthorisationServerMetadata() {
            @Override
            public String issuer() {
                return appUri;
            }

            @Override
            public String authorisationEndpoint() {
                return Path.of(appUri, "/oauth/authorize").toString();
            }

            @Override
            public String tokenEndpoint() {
                return Path.of(appUri, "/oauth/token").toString();
            }

            @Override
            public List<String> responseTypesSupported() {
                return List.of("code");
            }

            @Override
            public List<String> codeChallengeMethodsSupported() {
                return List.of("S256");
            }

            @Override
            public List<String> grantTypesSupported() {
                return List.of("authorization_code");
            }

            @Override
            public List<String> tokenEndpointAuthMethodsSupported() {
                return List.of("none");
            }

            @Override
            public boolean authorisationResponseIssParameterSupported() {
                return true;
            }
        };
    }

}
