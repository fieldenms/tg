package ua.com.fielden.platform.web.resources;

import org.restlet.routing.Router;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.file_reports.IReportDaoFactory;
import ua.com.fielden.platform.web.factories.ReportResourceFactory;

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

    public void registerReportResource(final Router router, final RestServerUtil rsu) {
        router.attach("/users/{username}/report", new ReportResourceFactory(injector.getInstance(IReportDaoFactory.class), rsu, injector));
    }

    public Injector getInjector() {
        return injector;
    }

    public EntityFactory getFactory() {
        return factory;
    }

}
