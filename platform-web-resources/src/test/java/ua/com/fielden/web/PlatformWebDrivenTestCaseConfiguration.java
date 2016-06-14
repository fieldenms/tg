package ua.com.fielden.web;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.IDbDrivenTestCaseConfiguration;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.test.IWebDrivenTestCaseConfiguration;

/**
 * A platform specific implementation for web-driven test configuration.
 *
 * @author TG Team
 *
 */
public class PlatformWebDrivenTestCaseConfiguration implements IWebDrivenTestCaseConfiguration {
    private final Injector injector;
    private final EntityFactory entityFactory;
    private RestServerUtil restServerUtil;

    public PlatformWebDrivenTestCaseConfiguration() {
        injector = new ApplicationInjectorFactory().add(new NewUserNotifierMockBindingModule()).getInjector();
        entityFactory = injector.getInstance(EntityFactory.class);
    }

    @Override
    public EntityFactory entityFactory() {
        return entityFactory;
    }

    @Override
    public Injector injector() {
        return injector;
    }

    @Override
    public RestServerUtil restServerUtil() {
        return restServerUtil;
    }

    @Override
    public void setDbDrivenTestConfiguration(final IDbDrivenTestCaseConfiguration config) {
        restServerUtil = new RestServerUtil(config.getInjector().getInstance(ISerialiser.class));
    }

}
