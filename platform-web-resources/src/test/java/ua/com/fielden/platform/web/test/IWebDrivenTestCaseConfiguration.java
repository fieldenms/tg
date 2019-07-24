package ua.com.fielden.platform.web.test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.test.IDbDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Contract for configuration used by web driven test cases, which are derived from {@link WebBasedTestCase}.
 * 
 * @author TG Team
 * 
 */
public interface IWebDrivenTestCaseConfiguration {
    final static int PORT = 9042;

    EntityFactory entityFactory();

    Injector injector();

    RestServerUtil restServerUtil();

    void setDbDrivenTestConfiguration(IDbDrivenTestCaseConfiguration config);
}
