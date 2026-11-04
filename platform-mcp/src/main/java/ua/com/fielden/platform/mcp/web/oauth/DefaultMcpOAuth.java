package ua.com.fielden.platform.mcp.web.oauth;

import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.McpConfig;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.List;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.utils.MiscUtilities.uriPath;

public class DefaultMcpOAuth implements IMcpOAuth {

    private final String appUri;
    private final McpConfig mcpConfig;
    private final McpOAuthClientRegistry clientRegistry;

    @Inject
    protected DefaultMcpOAuth(final @AppUri String appUri, final McpConfig mcpConfig, final McpOAuthClientRegistry clientRegistry) {
        this.appUri = StringUtils.stripEnd(appUri, "/");
        this.mcpConfig = mcpConfig;
        this.clientRegistry = clientRegistry;
    }

    @Override
    public String protectedResourceMetadataPath() {
        return uriPath("/.well-known/oauth-protected-resource", mcpConfig.resourcePath());
    }

    @Override
    public IProtectedResourceMetadata protectedResourceMetadata() {
        return new IProtectedResourceMetadata() {
            @Override
            public String resource() {
                return uriPath(appUri, mcpConfig.resourcePath());
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
                return uriPath(appUri, McpOAuthIocModule.AUTHORISATION_ENDPOINT_PATH);
            }

            @Override
            public String tokenEndpoint() {
                return uriPath(appUri, McpOAuthIocModule.TOKEN_ENDPOINT_PATH);
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

    @Override
    public Result validateClient(@Nullable final String clientId) {
        if (clientId == null) {
            return failure("Missing client identifier.");
        }
        return clientRegistry.find(clientId)
                .map(Result::successful)
                .orElseGet(() -> failuref("Unknown client [%s].", clientId));
    }

    @Override
    public Result validateClient(@Nullable final String clientId, @Nullable final String redirectUri) {
        final var result = validateClient(clientId);
        if (!result.isSuccessful()) {
            return result;
        }
        if (redirectUri == null) {
            return failure("Missing redirect URI.");
        }
        final McpOAuthClient client = result.getInstanceOrElseThrow();
        if (!client.isRedirectUriRegistered(redirectUri)) {
            return failuref("Redirect URI [%s] is not registered for client [%s].", redirectUri, clientId);
        }
        return result;
    }

}
