package ua.com.fielden.platform.mcp.test_config;

import com.google.inject.Injector;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.mcp.web.oauth.McpOAuthIocModule;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

import java.util.Properties;

/// A test configuration for OAuth-based authentication of MCP requests.
///
/// This is [TgMcpServerTestCaseConfiguration] with [McpOAuthIocModule] installed.
/// Requests to the MCP resource are still authenticated by [TestMcpAuthenticationModel].
/// A single client is registered, with [#TEST_CLIENT_ID] and [#TEST_CLIENT_REDIRECT_URI].
///
public final class TgMcpOAuthTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {

    public static final String TEST_CLIENT_ID = "test_client";
    public static final String TEST_CLIENT_REDIRECT_URI = "http://localhost/callback";

    private final Injector injector;

    public TgMcpOAuthTestCaseConfiguration(final Properties properties) {
        final var appProperties = TgMcpServerTestCaseConfiguration.getProperties(properties);
        appProperties.setProperty("mcp.clientRegistry.test.clientId", TEST_CLIENT_ID);
        appProperties.setProperty("mcp.clientRegistry.test.redirectUri", TEST_CLIENT_REDIRECT_URI);
        injector = new ApplicationInjectorFactory()
                .addAll(TgMcpServerTestCaseConfiguration.iocModules(appProperties))
                .add(new McpOAuthIocModule(appProperties))
                .getInjector();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

}
