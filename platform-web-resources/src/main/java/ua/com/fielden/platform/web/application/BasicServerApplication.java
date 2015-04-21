package ua.com.fielden.platform.web.application;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.web.factories.ResourceGuard;
import ua.com.fielden.platform.web.factories.SecurityTokenResourceFactory;
import ua.com.fielden.platform.web.factories.TokenRoleAssociationResourceFactory;
import ua.com.fielden.platform.web.factories.UserRoleAssociationResourceFactory;
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
    protected final ChallengeAuthenticator authenticator;

    private transient final Logger logger;

    private BasicServerApplication(
            final String securityRealm,
            final ChallengeAuthenticator authenticator,
            final Context context,
            final Injector injector,
            final EntityFactory factory,
            final RestServerUtil serverRestUtil,
            final String attachmentLocation,
            final Class<IEntityDao>[] controllerTypes) {
        super(context);
        if (securityRealm != null && authenticator != null) {
            throw new IllegalArgumentException("Either security realm or an authenticator should be provided, but not both");
        }
        this.securityRealm = securityRealm;
        this.authenticator = authenticator;
        this.injector = injector;
        helper = new RouterHelper(injector, factory);
        this.serverRestUtil = serverRestUtil;
        this.attachmentLocation = attachmentLocation;
        this.controllerTypes = controllerTypes;
        logger = Logger.getLogger(this.getClass());
    }

    public BasicServerApplication(final String securityRealm, final Context context, final Injector injector, final EntityFactory factory, final RestServerUtil serverRestUtil, final String attachmentLocation, final Class<IEntityDao>[] controllerTypes) {
        this(securityRealm, null, context, injector, factory, serverRestUtil, attachmentLocation, controllerTypes);
    }

    public BasicServerApplication(final ChallengeAuthenticator authenticator, final Context context, final Injector injector, final EntityFactory factory, final RestServerUtil serverRestUtil, final String attachmentLocation, final Class<IEntityDao>[] controllerTypes) {
        this(null, authenticator, context, injector, factory, serverRestUtil, attachmentLocation, controllerTypes);
    }

    @Override
    public final Restlet createInboundRoot() {
        // create resource router and attach all relevant resources to it
        final Router routerForResources = new Router(getContext());

        // register "platform" type controllers
        helper.register(routerForResources, IUserRoleDao.class);
        helper.register(routerForResources, IUserDao.class);
        helper.register(routerForResources, IEntityAttachmentAssociationController.class);
        helper.register(routerForResources, IEntityCentreAnalysisConfig.class);
        helper.register(routerForResources, IMainMenu.class);
        // TODO what about IMainMenuItemController or IEntityCentreConfigController? Or any other types?

        // register standard entity resources
        for (final Class<IEntityDao> daoType : controllerTypes) {
            helper.register(routerForResources, daoType);
        }
        try {
            helper.registerReportResource(routerForResources, serverRestUtil);
        } catch (final Exception e) {
            logger.debug("Could not register a report resource.", e);
        }
        // register user role association resource
        routerForResources.attach("/users/{username}/useroles", new UserRoleAssociationResourceFactory(injector));
        // register role token association resource
        final Restlet securityTokenRestlet = new SecurityTokenResourceFactory(injector);
        routerForResources.attach("/users/{username}/securitytokens", securityTokenRestlet);
        routerForResources.attach("/users/{username}/securitytokens/{token}", securityTokenRestlet); // authorisation resources
        routerForResources.attach("/users/{username}/securitytokens/{token}/useroles", securityTokenRestlet);
        final Restlet tokenRoleAssociationRestlet = new TokenRoleAssociationResourceFactory(injector);
        routerForResources.attach("/users/{username}/tokenroleassociation", tokenRoleAssociationRestlet);

        // register resource for handling entity aggregation and generated type related requests
        helper.registerAggregates(routerForResources);
        helper.registerGeneratedTypeResources(routerForResources);

        /////////////////////////////////////////////////////////////
        /////////////// application specific resources //////////////
        /////////////////////////////////////////////////////////////
        registerApplicationResources(routerForResources);
        /////////////////////////////////////////////////////////////

        // create resource guard and associate it with the resource router
        final ChallengeAuthenticator guard;

        if (securityRealm != null) {
            guard = new ResourceGuard(getContext(), securityRealm, serverRestUtil, injector);
        } else {
            guard = authenticator;
        }
        guard.setNext(routerForResources);

        final Router mainRouter = new Router(getContext());
        // FIXME Insecure resource!!!
        helper.registerAttachment(mainRouter, attachmentLocation);
        mainRouter.attach(guard);

        // register snappy query resource
        helper.registerSnappyQueryResource(mainRouter);

        return mainRouter;
    }

    public static Class<IEntityDao>[] companionObjectTypes(final List<Class<? extends AbstractEntity<?>>> domainTypes) {
        final List<Class<? extends IEntityDao>> companionTypes = new ArrayList<Class<? extends IEntityDao>>();

        for (final Class<? extends AbstractEntity<?>> entityType : domainTypes) {
            companionTypes.add(CompanionObjectAutobinder.companionObjectType(entityType));
        }

        return companionTypes.toArray(new Class[] {});
    }

    /**
     * Should be implemented for concrete application web resource management. The provided router should be used to bind application resources.
     *
     * @param routerForResources
     */
    protected abstract void registerApplicationResources(final Router routerForResources);
}
