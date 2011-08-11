package ua.com.fielden.platform.web.test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.test.IDbDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;


/**
 * Contract for configuration used by web driven test cases, which are derived from {@link WebBasedTestCase}.
 *
 * @author TG Team
 *
 */
public interface IWebDrivenTestCaseConfiguration {
    final static int PORT = 9000;

    EntityFactory entityFactory();
    Injector injector();
    RestClientUtil restClientUtil();
    RestServerUtil restServerUtil();

    void setDbDrivenTestConfiguration(IDbDrivenTestCaseConfiguration config);
}
