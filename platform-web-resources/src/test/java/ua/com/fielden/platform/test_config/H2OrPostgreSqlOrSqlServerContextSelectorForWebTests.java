package ua.com.fielden.platform.test_config;

import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.runners.AbstractDomainDrivenTestCaseRunner;
import ua.com.fielden.platform.test.runners.H2DomainDrivenTestCaseRunner;
import ua.com.fielden.platform.test.runners.H2DomainDrivenTestCaseRunner.H2TestContext;
import ua.com.fielden.platform.test.runners.PostgresqlDomainDrivenTestCaseRunner;
import ua.com.fielden.platform.test.runners.PostgresqlDomainDrivenTestCaseRunner.PostgresqlTestContext;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner.SqlServerTestContext;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.test.server.DataPopulationConfigForWebTests;

import java.util.Optional;
import java.util.Properties;

/// A test runner that selects a test configuration [ITestContext] from [SqlServerDomainDrivenTestCaseRunner] or [PostgresqlDomainDrivenTestCaseRunner] for running unit test.
/// The criteria for selecting the appropriate test runner is based on runtime settings.
///
/// This test runner is capable for running tests with Web UI infrastructure such as [IWebUiConfig].
/// Used for web-server-driven tests in *-web-resources module.
///
public class H2OrPostgreSqlOrSqlServerContextSelectorForWebTests extends H2OrPostgreSqlOrSqlServerContextSelector {

    public H2OrPostgreSqlOrSqlServerContextSelectorForWebTests(Class<?> klass) throws Exception {
        super(klass);
    }

    public H2OrPostgreSqlOrSqlServerContextSelectorForWebTests(Class<?> klass, Class<? extends DbCreator> dbCreatorType, Optional<IDomainDrivenTestCaseConfiguration> testConfig) throws Exception {
        super(klass, dbCreatorType, testConfig);
    }

    private static class PostgresqlTestContextForWebTests extends PostgresqlTestContext {
        @Override
        public Properties mkDbProps(String dbUri) {
            return mkPropsWebCapable(super.mkDbProps(dbUri));
        }
    }

    private static class SqlServerTestContextForWebTests extends SqlServerTestContext {
        @Override
        public Properties mkDbProps(String dbUri) {
            return mkPropsWebCapable(super.mkDbProps(dbUri));
        }
    }

    private static class H2TestContextForWebTests extends H2TestContext {
        public H2TestContextForWebTests(AbstractDomainDrivenTestCaseRunner runner) {
            super(runner);
        }
        @Override
        public Properties mkDbProps(String dbUri) {
            return mkPropsWebCapable(super.mkDbProps(dbUri));
        }
    }

    private static Properties mkPropsWebCapable(Properties props) {
        props.setProperty("config.domain", DataPopulationConfigForWebTests.class.getName());
        props.setProperty("attachments.location", "../platform-web-resources/src/test/resources/attachments");
        props.setProperty("web.domain", "unit-test.com");
        props.setProperty("web.path", "/");
        props.setProperty("port", "1234");
        return props;
    }

    protected ITestContext selectRunnerConfig() {
        return POSTGRESQL
                ? new PostgresqlTestContextForWebTests()
                : (SQL_SERVER ? new SqlServerTestContextForWebTests() : new H2TestContextForWebTests(this));
    }

}
