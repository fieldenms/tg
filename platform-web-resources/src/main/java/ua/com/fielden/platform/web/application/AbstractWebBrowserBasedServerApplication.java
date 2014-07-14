package ua.com.fielden.platform.web.application;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.routing.Router;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.web.CentreResourceFactory;
import ua.com.fielden.platform.web.FileResourceFactory;
import ua.com.fielden.platform.web.QueryPageResourceFactory;

import com.google.inject.Injector;

public abstract class AbstractWebBrowserBasedServerApplication extends Application {
    private final Injector injector;
    private final String username;

    public AbstractWebBrowserBasedServerApplication(final Context context, final Injector injector, final String name, final String desc, final String owner, final String author, final String username) {
        super(context);
        this.username = username;
        this.injector = injector;
        setName(name);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }

    @Override
    public final Restlet createInboundRoot() {
        final IEntityCentreConfigController eccc = injector.getInstance(IEntityCentreConfigController.class);
        final ISerialiser serialiser = injector.getInstance(ISerialiser.class);
        final Router router = new Router(getContext());

        attachAdditionalResources(router);

        router.attach("/centre.js", new FileResourceFactory("../../tg/platform-ui/src/main/java/ua/com/fielden/platform/web/centre/centre.js", MediaType.TEXT_JAVASCRIPT));
        router.attach("/centre.css", new FileResourceFactory("../../tg/platform-ui/src/main/java/ua/com/fielden/platform/web/centre/centre.css", MediaType.TEXT_CSS));
        router.attach("/main", new FileResourceFactory("../../tg/platform-ui/src/main/java/ua/com/fielden/platform/web/centre/main.html", MediaType.TEXT_HTML));
        router.attach("/centre", new FileResourceFactory("../../tg/platform-ui/src/main/java/ua/com/fielden/platform/web/centre/centre.html", MediaType.TEXT_HTML));
        router.attach("/centre/{centreName}", new CentreResourceFactory(eccc, serialiser, username));
        router.attach("/centre/{centreName}/query/{page}", new QueryPageResourceFactory(injector, username));
        return router;
    }

    protected abstract void attachAdditionalResources(final Router router);
    //     router.attach("/", new FileResourceFactory("tg-app-example.html", MediaType.TEXT_HTML)); // TODO application-specific stuff
}
