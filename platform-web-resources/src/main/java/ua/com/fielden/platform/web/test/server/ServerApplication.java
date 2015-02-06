package ua.com.fielden.platform.web.test.server;

import org.restlet.Context;
import org.restlet.routing.Router;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.web.application.BasicServerApplication;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * Server application for Web UI Testing Server, which should override method {@link #registerApplicationResources(Router)} to provide application specific binding of web
 * resources.
 *
 * @author TG Team
 */
public class ServerApplication extends BasicServerApplication {

    public ServerApplication(final String securityRealm, final Context context, final Injector injector, final EntityFactory factory, final RestServerUtil serverRestUtil, final String attachmentLocation, final Class<IEntityDao>[] controllerTypes) {
        super(securityRealm, context, injector, factory, serverRestUtil, attachmentLocation, controllerTypes);
    }

    @Override
    protected void registerApplicationResources(final Router routerForResources) {
    }
}
