package ua.com.fielden.platform.web.resources.webui;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity validation serves as a back-end mechanism of changing entity properties and validating that changes.
 *
 * The server does not keep any state about the entities to be modified.
 *
 * @author TG Team
 *
 */
public class EntityValidationResource<T extends AbstractEntity<?>> extends ServerResource {
    private final Class<T> entityType;
    private final EntityFactory entityFactory;
    private final RestServerUtil restUtil;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder companionFinder;
    private final IWebUiConfig webUiConfig;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityValidationResource(
            final Class<T> entityType,
            final EntityFactory entityFactory,
            final RestServerUtil restUtil,
            final ICriteriaGenerator critGenerator,
            final ICompanionObjectFinder companionFinder,
            final IWebUiConfig webUiConfig,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.entityType = entityType;
        this.entityFactory = entityFactory;
        this.restUtil = restUtil;
        this.critGenerator = critGenerator;
        this.companionFinder = companionFinder;
        this.webUiConfig = webUiConfig;
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    public Representation validate(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("ENTITY_VALIDATION_RESOURCE: validate started.");
            // NOTE: the following line can be the example how 'entity validation' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during entity validation.");
            final SavingInfoHolder savingInfoHolder = EntityResourceUtils.restoreSavingInfoHolder(envelope, restUtil);

            final T applied = EntityResource.restoreEntityFrom(savingInfoHolder, entityType, entityFactory, webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, 0);

            logger.debug("ENTITY_VALIDATION_RESOURCE: validate finished.");
            return restUtil.rawListJSONRepresentation(EntityResourceUtils.resetContextBeforeSendingToClient(applied));
        }, restUtil);
    }
}
