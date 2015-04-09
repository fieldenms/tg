package ua.com.fielden.platform.web.test.server;

import org.restlet.Context;
import org.restlet.routing.Router;

import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.application.AbstractWebApplication;

import com.google.inject.Injector;

/**
 * Custom {@link AbstractWebApplication} descendant for Web UI Testing Server. Provided in order to configure entity centres, masters and other client specific stuff.
 *
 * @author TG Team
 *
 */
public class WebApplication extends AbstractWebApplication {

    /**
     * Creates an instance of {@link WebApplication} (for more information about the meaning of all this arguments see {@link AbstractWebApplication#AbstractWebApp}
     *
     * @param context
     * @param injector
     * @param resourcePaths
     * @param name
     * @param desc
     * @param owner
     * @param author
     * @param username
     */
    public WebApplication(
            final Context context,
            final Injector injector,
            final String name,
            final String desc,
            final String owner,
            final String author,
            final IWebApp webApp) {
        super(context, injector, new String[0], name, desc, owner, author, webApp);
    }

    @Override
    protected void attachFunctionalEntities(final Router router, final Injector injector) {
        // router.attach("/users/{username}/CustomFunction", new FunctionalEntityResourceFactory<CustomFunction, ICustomFunction>(ICustomFunction.class, injector));
    }
}
