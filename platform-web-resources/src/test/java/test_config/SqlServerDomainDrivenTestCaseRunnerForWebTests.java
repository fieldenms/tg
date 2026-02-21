package test_config;

import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner;
import ua.com.fielden.platform.web.test.server.DataPopulationConfig;
import ua.com.fielden.platform.web.test.server.DataPopulationConfigForWebTests;

import java.util.Properties;

/// A test case runner for domain-driven unit tests extending [AbstractDomainDrivenTestCase].
///
/// Used for web-server-driven tests in *-web-resources module.
///
public class SqlServerDomainDrivenTestCaseRunnerForWebTests extends SqlServerDomainDrivenTestCaseRunner {
    
    public SqlServerDomainDrivenTestCaseRunnerForWebTests(final Class<?> klass) throws Exception {
        super(klass);
    }
    
    public SqlServerDomainDrivenTestCaseRunnerForWebTests(final Class<?> klass, final IDomainDrivenTestCaseConfiguration testConfig) throws Exception {
        super(klass, testConfig);
    }
    
    @Override
    protected Properties mkDbProps(final String dbUri) {
        final Properties props = super.mkDbProps(dbUri);
        props.setProperty("config.domain", DataPopulationConfigForWebTests.class.getName());
        props.setProperty("attachments.location", "../platform-web-resources/src/test/resources/attachments");
        props.setProperty("web.domain", "unit-test.com");
        props.setProperty("web.path", "/");
        props.setProperty("port", "1234");
        return props;
    }
    
}