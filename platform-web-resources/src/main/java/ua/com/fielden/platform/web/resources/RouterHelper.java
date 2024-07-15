package ua.com.fielden.platform.web.resources;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Provides convenient methods for routing standard entity resources.
 *
 * @author TG Team
 *
 */
public final class RouterHelper {
    private final Injector injector;
    private final EntityFactory factory;

    public RouterHelper(final Injector injector, final EntityFactory factory) {
        this.injector = injector;
        this.factory = factory;
    }

    public Injector getInjector() {
        return injector;
    }

    public EntityFactory getFactory() {
        return factory;
    }

}
