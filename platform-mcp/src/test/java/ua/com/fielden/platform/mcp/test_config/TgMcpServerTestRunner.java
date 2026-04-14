package ua.com.fielden.platform.mcp.test_config;

import org.junit.runner.RunWith;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;

import java.util.Properties;

/// JUnit test runner for testing of the TG MCP server.
///
/// The base class for web resource tests is [AbstractTgMcpServerTestCase].
/// Test classes can also be created without extending that base class, but then they must be annotated with [RunWith] specifying this runner.
///
/// IoC configuration is provided by [TgMcpServerTestCaseConfiguration].
///
public class TgMcpServerTestRunner extends H2OrPostgreSqlOrSqlServerContextSelector {

    public TgMcpServerTestRunner(final Class<?> klass) throws Exception {
        super(klass);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final var props = super.mkDbProps(dbUri);
        props.setProperty("config.domain", TgMcpServerTestCaseConfiguration.class.getName());
        return props;
    }

}
