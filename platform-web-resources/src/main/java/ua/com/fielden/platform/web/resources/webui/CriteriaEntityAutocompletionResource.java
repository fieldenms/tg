package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.IContextDecomposer.AUTOCOMPLETE_ACTIVE_ONLY_KEY;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCentreContext;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityWithoutConflicts;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaValidationPrototype;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalPropertyName;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class CriteriaEntityAutocompletionResource<T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> extends AbstractWebResource {
    private static final String AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY = "@@flagChanged";
    private static final String CENTRE_DIRTY_KEY = "@@centreDirty";
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    private final String criterionPropertyName;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<T> centre;

    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final EntityFactory entityFactory;
    private final ICentreConfigSharingModel sharingModel;

    private final Logger logger = Logger.getLogger(getClass());

    public CriteriaEntityAutocompletionResource(
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final Optional<String> saveAsName,
            final String criterionPropertyName,
            final EntityCentre<T> centre,
            final RestServerUtil restUtil,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final ICentreConfigSharingModel sharingModel,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.miType = miType;
        this.saveAsName = saveAsName;
        this.criterionPropertyName = criterionPropertyName;
        this.restUtil = restUtil;
        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
        this.centre = centre;

        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.entityFactory = entityFactory;
        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.sharingModel = sharingModel;
    }

    /**
     * Handles POST request resulting from tg-entity-search-criteria's / tg-entity-editor's (both they are used as criteria editors in centres) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            // logger.debug("CRITERIA_ENTITY_AUTOCOMPLETION_RESOURCE: search started.");
            //            // NOTE: the following line can be the example how 'entity search' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during criteria entity searching.");
            final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);
            final User user = userProvider.getUser();
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            final M criteriaEntity;
            final M enhancedCentreEntityQueryCriteria;
            final Class<M> criteriaType;
            final Map<String, Object> modifHolder = !centreContextHolder.proxiedPropertyNames().contains("modifHolder") ? centreContextHolder.getModifHolder() : new HashMap<>();
            if (CentreResourceUtils.isEmpty(modifHolder)) {
                // this branch is used for criteria entity generation to get the type of that entity later -- the modifiedPropsHolder is empty (no 'selection criteria' is needed in the context).
                criteriaEntity = null;
                enhancedCentreEntityQueryCriteria = createCriteriaValidationPrototype(
                    miType, saveAsName,
                    updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
                    companionFinder, critGenerator, 0L,
                    user,
                    device(),
                    domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel
                );
                criteriaType = (Class<M>) enhancedCentreEntityQueryCriteria.getClass();
            } else {
                criteriaEntity = (M) createCriteriaEntityWithoutConflicts(modifHolder, companionFinder, critGenerator, miType, saveAsName, user, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
                enhancedCentreEntityQueryCriteria = criteriaEntity;
                criteriaType = (Class<M>) criteriaEntity.getClass();
            }

            // TODO criteriaType is necessary to be used for 1) value matcher creation 2) providing value matcher fetch model
            // Please, investigate whether such items can be done without 'criteriaType', and this will eliminate the need to create 'criteriaEntity' (above).

            final Pair<IValueMatcherWithCentreContext<T>, Optional<CentreContextConfig>> valueMatcherAndContextConfig;
            if (centre != null) {
                valueMatcherAndContextConfig = centre.<T> createValueMatcherAndContextConfig(criteriaType, criterionPropertyName);
            } else {
                final String msg = String.format("No EntityCentre instance can be found for already constructed 'criteria entity' with type [%s].", criteriaType.getName());
                logger.error(msg);
                throw new IllegalStateException(msg);
            }

            final IValueMatcherWithCentreContext<T> valueMatcher = valueMatcherAndContextConfig.getKey();
            final Optional<CentreContextConfig> contextConfig = valueMatcherAndContextConfig.getValue();

            // create context, if any
            final Optional<CentreContext<T, ?>> context = createCentreContext(
                true, // full context, fully-fledged restoration. This means that IValueMatcherWithCentreContext descendants (centre matchers) could use IContextDecomposer for context decomposition on deep levels.
                webUiConfig,
                companionFinder,
                user,
                critGenerator,
                entityFactory,
                centreContextHolder,
                criteriaEntity,
                contextConfig,
                criterionPropertyName,
                device(),
                domainTreeEnhancerCache,
                eccCompanion,
                mmiCompanion,
                userCompanion,
                sharingModel
            );
            if (context.isPresent()) {
                // logger.debug("context for prop [" + criterionPropertyName + "] = " + context);
                valueMatcher.setContext(context.get());
            } else {
                // TODO check whether such setting is needed (need to test autocompletion in centres without that setting) or can be removed:
                valueMatcher.setContext(new CentreContext<>());
            }

            final Optional<Boolean> activeOnlyOpt;
            final Optional<Boolean> centreDirtyOpt;
            final Boolean activeOnlyFromClient = (Boolean) centreContextHolder.getCustomObject().get(AUTOCOMPLETE_ACTIVE_ONLY_KEY);
            if (valueMatcher.getFetch() != null && isActivatableEntityType(valueMatcher.getFetch().getEntityType())) { // fetch is not defined only for property descriptors, see createValueMatcherAndContextConfig
                final Class<T> entityType = getEntityType(miType);
                final String origPropName = getOriginalPropertyName(criteriaType, criterionPropertyName);
                ofNullable(activeOnlyFromClient).ifPresent(activeOnly -> {
                    enhancedCentreEntityQueryCriteria.adjustCentre(centreManager -> {
                        centreManager.getFirstTick().setAutocompleteActiveOnly(entityType, origPropName, activeOnly);
                    });
                });
                activeOnlyOpt = of(enhancedCentreEntityQueryCriteria.freshCentre().getFirstTick().getAutocompleteActiveOnly(entityType, origPropName));
                final Map<String, Object> customObject = new LinkedHashMap<>(valueMatcher.getContext().getCustomObject());
                customObject.put(AUTOCOMPLETE_ACTIVE_ONLY_KEY, activeOnlyOpt.get());
                valueMatcher.getContext().setCustomObject(customObject);
                centreDirtyOpt = activeOnlyFromClient != null ? of(enhancedCentreEntityQueryCriteria.isCentreDirty()) : empty();
            } else {
                activeOnlyOpt = empty();
                centreDirtyOpt = empty();
            }

            // prepare the search string and perform value matching
            final T2<String, Integer> searchStringAndDataPageNo = EntityAutocompletionResource.prepSearchString(centreContextHolder, false);
            final List<? extends AbstractEntity<?>> entities =  valueMatcher.findMatchesWithModel(searchStringAndDataPageNo._1, searchStringAndDataPageNo._2);

            // logger.debug("CRITERIA_ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJsonRepresentationWithoutIdAndVersion(entities,
                activeOnlyOpt.map(
                    activeOnly -> AUTOCOMPLETE_ACTIVE_ONLY_KEY + ":" + activeOnly
                    + "," + AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY + ":" + (activeOnlyFromClient != null)
                    + centreDirtyOpt.map(centreDirty -> "," + CENTRE_DIRTY_KEY + ":" + centreDirty).orElse("")
                ));
        }, restUtil);
    }

}