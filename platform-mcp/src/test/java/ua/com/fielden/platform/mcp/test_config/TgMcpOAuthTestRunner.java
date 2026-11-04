package ua.com.fielden.platform.mcp.test_config;

import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;

import java.util.Properties;

/// JUnit test runner for testing of OAuth-based authentication of MCP requests.
///
/// The base class for such tests is [AbstractTgMcpOAuthTestCase].
///
/// IoC configuration is provided by [TgMcpOAuthTestCaseConfiguration].
///
public class TgMcpOAuthTestRunner extends H2OrPostgreSqlOrSqlServerContextSelector {

    public TgMcpOAuthTestRunner(final Class<?> klass) throws Exception {
        super(klass);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final var props = super.mkDbProps(dbUri);
        props.setProperty("config.domain", TgMcpOAuthTestCaseConfiguration.class.getName());
        return props;
    }

}
