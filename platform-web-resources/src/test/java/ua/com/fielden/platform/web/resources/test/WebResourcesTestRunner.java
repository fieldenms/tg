package ua.com.fielden.platform.web.resources.test;

import org.junit.runner.RunWith;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;
import ua.com.fielden.platform.web.resources.webui.AbstractWebResourceWithDaoTestCase;

import java.util.Properties;

/// JUnit test runner for testing of web resources.
///
/// The base class for web resource tests is [AbstractWebResourceWithDaoTestCase].
/// Test classes can also be created without extending this base class, but then they must be annotated with [RunWith] specifying this runner.
///
/// The IoC configuration used by this runner enhances the configuration of `platform-dao` with Web UI bindings that web resources typically depend on.
/// If it is discovered that some IoC bindings are missing, this runner's IoC configuration should be adjusted accordingly.
///
public class WebResourcesTestRunner extends H2OrPostgreSqlOrSqlServerContextSelector {

    public WebResourcesTestRunner(final Class<?> klass) throws Exception {
        super(klass);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final var props = super.mkDbProps(dbUri);
        props.setProperty("config.domain", WebResourcesTestCaseConfiguration.class.getName());
        return props;
    }

}
