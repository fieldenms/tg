package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyApplicationErrorHandler;

import java.util.Map;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.restoreEntityFrom;
import static ua.com.fielden.platform.web.resources.webui.MultiActionUtils.createPropertyActionIndicesForMaster;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreSavingInfoHolder;

/**
 * The web resource for entity validation serves as a back-end mechanism of changing entity properties and validating that changes.
 *
 * The server does not keep any state about the entities to be modified.
 *
 * @author TG Team
 *
 */
public class EntityValidationResource<T extends AbstractEntity<?>> extends AbstractWebResource {
    static final String VALIDATION_COUNTER = "@validationCounter";
    private final Class<T> entityType;
    private final EntityFactory entityFactory;
    private final RestServerUtil restUtil;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder companionFinder;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final ICentreConfigSharingModel sharingModel;
    private final Logger logger = LogManager.getLogger(getClass());

    public EntityValidationResource(
            final Class<T> entityType,
            final EntityFactory entityFactory,
            final RestServerUtil restUtil,
            final ICriteriaGenerator critGenerator,
            final ICompanionObjectFinder companionFinder,
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICentreConfigSharingModel sharingModel,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.entityType = entityType;
        this.entityFactory = entityFactory;
        this.restUtil = restUtil;
        this.critGenerator = critGenerator;
        this.companionFinder = companionFinder;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.sharingModel = sharingModel;
    }

    /**
     * Handles POST request resulting from tg-entity-master <code>validate()</code> method.
     */
    @Post
    public Representation validate(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("ENTITY_VALIDATION_RESOURCE: validate started.");
            // NOTE: the following line can be the example how 'entity validation' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during entity validation.");
            final SavingInfoHolder savingInfoHolder = restoreSavingInfoHolder(envelope, restUtil);

            final User user = userProvider.getUser();
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            final T applied = restoreEntityFrom(false, savingInfoHolder, entityType, PropertyApplicationErrorHandler.standard, entityFactory, webUiConfig, companionFinder, user, critGenerator, 0, device(), eccCompanion, mmiCompanion, userCompanion, sharingModel);

            logger.debug("ENTITY_VALIDATION_RESOURCE: validate finished.");
            final Result result = restUtil.singleEntityResult(applied);
            final Map<String, Object> customObject = linkedMapOf(t2(VALIDATION_COUNTER, savingInfoHolder.getModifHolder().get(VALIDATION_COUNTER)), createPropertyActionIndicesForMaster(applied, webUiConfig)); // savingInfoHolder and its modifHolder are never empty
            return restUtil.resultJSONRepresentation(result.extendResultWithCustomObject(customObject));
        }, restUtil);
    }
}
