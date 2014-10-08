package ua.com.fielden.platform.web.application;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.web.CentreResourceFactory;
import ua.com.fielden.platform.web.FileResourceFactory;
import ua.com.fielden.platform.web.MainWebApplicationResourceFactory;
import ua.com.fielden.platform.web.WebApplication;

import com.google.inject.Injector;

public abstract class AbstractWebBrowserBasedServerApplication extends Application {
    private final Injector injector;
    // TODO when authentication mechanism will be implemented then user name won't be needed any longer.
    private final String username;
    private final Set<String> resourcePaths = new LinkedHashSet<>();
    private final Logger logger = Logger.getLogger(getClass());

    public AbstractWebBrowserBasedServerApplication(final Context context, final Injector injector, final String[] resourcePaths, final String name, final String desc, final String owner, final String author, final String username) {
        super(context);
//        this.platformJsScriptsLocation = "../../tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/";
//        this.platformVendorJsScriptsLocation = "../../tg/platform-web-ui/src/main/resources/";
        // --> TODO not so elegant and flexible. There should be more elegant version for development and deployment. Use application.props file.
//        this.platformGisJsScriptsLocation = platformJsScriptsLocation + "gis/";
        // --> TODO not so elegant and flexible. There should be more elegant version for development and deployment. Use application.props file.
        this.username = username;
        this.injector = injector;
        this.resourcePaths.addAll(Arrays.asList("", "ua/com/fielden/platform/web/"));
        this.resourcePaths.addAll(Arrays.asList(resourcePaths));
        setName(name);
        setDescription(desc);
        setOwner(owner);
        setAuthor(author);
    }

    @Override
    public final Restlet createInboundRoot() {
	// Create router and web application for registering resources.
	final Router router = new Router(getContext());
	final WebApplication webApplication = new WebApplication(getName());

	// Initialise web application with entity centres, entity masters and other custom views.
	initWebApplication(webApplication);

	// Attach main application resource.
	router.attach("/", new MainWebApplicationResourceFactory(webApplication));

	// Registering entity centres.
	attachCentreResources(router, webApplication);
	// TODO Register entity masters and other custom views.

	// Register resources those are in resource paths.
	attacheResoureces(router);

        attachAdditionalResources(router);
        return router;
    }

    private void attachCentreResources(final Router router, final WebApplication webApplication) {
        logger.info("\t\tCentre resources attaching...");
        router.attach("/centre/{centreName}", new CentreResourceFactory(webApplication.getCentres(), username, injector));
    }

    private void attacheResoureces(final Router router) {
	logger.info("\t\tResources attaching for:..." + "\n\t\t" + StringUtils.join(resourcePaths, "/\n\t\t") + "/");
	router.attach("/vendor/{filePath}", new FileResourceFactory(Collections.unmodifiableSet(resourcePaths)));
    }


    protected Logger logger() {
        return logger;
    }

    protected abstract void attachAdditionalResources(final Router router);

    protected abstract void initWebApplication(WebApplication app);
}