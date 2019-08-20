package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.resources.webui.EntityResource.restoreEntityFrom;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreSavingInfoHolder;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity validation serves as a back-end mechanism of changing entity properties and validating that changes.
 *
 * The server does not keep any state about the entities to be modified.
 *
 * @author TG Team
 *
 */
public class EntityValidationResource<T extends AbstractEntity<?>> extends AbstractWebResource {
    private final Class<T> entityType;
    private final EntityFactory entityFactory;
    private final RestServerUtil restUtil;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder companionFinder;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityValidationResource(
            final Class<T> entityType,
            final EntityFactory entityFactory,
            final RestServerUtil restUtil,
            final ICriteriaGenerator critGenerator,
            final ICompanionObjectFinder companionFinder,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider);
        
        this.entityType = entityType;
        this.entityFactory = entityFactory;
        this.restUtil = restUtil;
        this.critGenerator = critGenerator;
        this.companionFinder = companionFinder;
        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    public Representation validate(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("ENTITY_VALIDATION_RESOURCE: validate started.");
            // NOTE: the following line can be the example how 'entity validation' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during entity validation.");
            final SavingInfoHolder savingInfoHolder = restoreSavingInfoHolder(envelope, restUtil);
            
            final User user = userProvider.getUser();
            final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);
            
            final T applied = restoreEntityFrom(false, savingInfoHolder, entityType, entityFactory, webUiConfig, companionFinder, user, userProvider, critGenerator, 0, device(), domainTreeEnhancerCache, eccCompanion, mmiCompanion, userCompanion);
            
            logger.debug("ENTITY_VALIDATION_RESOURCE: validate finished.");
            return restUtil.rawListJSONRepresentation(applied);
        }, restUtil);
    }
}
