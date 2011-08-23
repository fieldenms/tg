package ua.com.fielden.platform.web.application;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.web.ResourceGuard;
import ua.com.fielden.platform.web.SecurityTokenResourceFactory;
import ua.com.fielden.platform.web.UserRoleAssociationResourceFactory;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.RouterHelper;

import com.google.inject.Injector;

/**
 * Basic implementation of the server side application. Provides all the essential of a typical TG-based served application resources.
 * <p>
 * This class should be inherited in order to provide application specific web resource binding (see method {@link #registerApplicationResources(Router)}).
 *
 * @author TG Team
 */
public abstract class BasicServerApplication extends Application {

    protected final String securityRealm;
    protected final Injector injector;
    protected final RestServerUtil serverRestUtil;
    protected final String attachmentLocation;
    protected final RouterHelper helper;
    protected final Class<IEntityDao>[] controllerTypes;

    public BasicServerApplication(final String securityRealm, final Context context, final Injector injector, final EntityFactory factory, final RestServerUtil serverRestUtil, final String attachmentLocation, final Class<IEntityDao>[] controllerTypes) {
	super(context);
	this.securityRealm = securityRealm;
	this.injector = injector;
	helper = new RouterHelper(injector, factory);
	this.serverRestUtil = serverRestUtil;
	this.attachmentLocation = attachmentLocation;
	this.controllerTypes = controllerTypes;
    }


    @Override
    public final Restlet createRoot() {
	// create resource router and attach all relevant resources to it
	final Router routerForResources = new Router(getContext());
	// register standard entity resources
	for (final Class<IEntityDao> daoType : controllerTypes) {
	    helper.register(routerForResources, daoType);
	}
	helper.registerReportResource(routerForResources);
	// register user role association resource
	routerForResources.attach("/users/{username}/useroles", new UserRoleAssociationResourceFactory(injector));
	// register role token association resource
	final Restlet tokenRoleAssociationRestlet = new SecurityTokenResourceFactory(injector);
	routerForResources.attach("/users/{username}/securitytokens", tokenRoleAssociationRestlet);
	routerForResources.attach("/users/{username}/securitytokens/{token}", tokenRoleAssociationRestlet); // authorisation resources
	routerForResources.attach("/users/{username}/securitytokens/{token}/useroles", tokenRoleAssociationRestlet);
	// register entity aggregation resource
	helper.registerAggregates(routerForResources);

	/////////////////////////////////////////////////////////////
	/////////////// application specific resources //////////////
	/////////////////////////////////////////////////////////////
	registerApplicationResources(routerForResources);
	/////////////////////////////////////////////////////////////

	// create resource guard and associate it with the resource router
	final ResourceGuard guard = new ResourceGuard(getContext(), securityRealm, serverRestUtil, injector);
	guard.setNext(routerForResources);

	final Router mainRouter = new Router(getContext());
	// FIXME Insecure resource!!!
	helper.registerAttachment(mainRouter, attachmentLocation);
	mainRouter.attach(guard);

	// register snappy query resource
	helper.registerSnappyQueryResource(mainRouter);

	return mainRouter;
    }


    /**
     * Should be implemented for concrete application web resource management. The provided router should be used to bind application resources.
     *
     * @param routerForResources
     */
    protected abstract void registerApplicationResources(final Router routerForResources);
}
