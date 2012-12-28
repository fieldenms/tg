package ua.com.fielden.platform.web.system;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.web.InfoResourceFactory;
import ua.com.fielden.platform.web.ReferenceDependencyDownloadResourceFactory;
import ua.com.fielden.platform.web.ReferenceDependencyListResourceFactory;
import ua.com.fielden.platform.web.ResourceGuard;
import ua.com.fielden.platform.web.UserAuthResourceFactory;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.RouterHelper;

import com.google.inject.Injector;

/**
 * This is a web application handling user authentication and application updates. All resources that form part of this application are unversioned and considered to be system resources.
 * <p>
 * These resources are unversioned by intrinsic nature of the basic requirement for a web client to be able to connetect to the application server regardless of the version in order handle things like application updates, user authentication (required for updates) etc.
 *
 * @author TG Team
 */
public final class SystemResources extends Application {

    private final String appInfo;
    private final Injector injector;
    private final String authenticationUri;
    private final RestServerUtil util;
    private final String referenceDependeciensLocation;
    private final RouterHelper helper;
    private final UserAuthResourceFactory uarFactory;

    public SystemResources(final Context context, final UserAuthResourceFactory uarFactory, final Injector injector, final EntityFactory factory, final RestServerUtil serverRestUtil, final String authenticationUri, final String referenceDependeciensLocation, final String appInfo) {
	super(context);
	this.injector = injector;
	this.util = serverRestUtil;
	this.authenticationUri = authenticationUri;
	this.referenceDependeciensLocation = referenceDependeciensLocation;
	helper = new RouterHelper(injector, factory);
	this.uarFactory = uarFactory;
	this.appInfo  = appInfo;
    }


    @Override
    public Restlet createInboundRoot() {
	// create resource router and attach all relevant resources to it
	final Router routerForResources = new Router(getContext());
	routerForResources.attach("/users/{username}/dependencies/{file-name}", new ReferenceDependencyDownloadResourceFactory(referenceDependeciensLocation, injector));
	routerForResources.attach("/users/{username}/update", new ReferenceDependencyListResourceFactory(referenceDependeciensLocation, injector));

	helper.register(routerForResources, IMainMenuItemController.class);
	helper.register(routerForResources, IMainMenuItemInvisibilityController.class);
	helper.register(routerForResources, IMainMenu.class);
	helper.register(routerForResources, IEntityMasterConfigController.class);
	helper.register(routerForResources, IEntityLocatorConfigController.class);
	helper.register(routerForResources, IEntityCentreConfigController.class);
	helper.register(routerForResources, IEntityCentreAnalysisConfig.class);

	// create resource guard and associate it with the resource router
	final ResourceGuard guard = new ResourceGuard(getContext(), "Fleet Pilot", util, injector);
	guard.setNext(routerForResources);

	final Router mainRouter = new Router(getContext());
	mainRouter.attach(authenticationUri, uarFactory);
	mainRouter.attach("/info", new InfoResourceFactory(appInfo));
	mainRouter.attach(guard);

	return mainRouter;
    }
}
