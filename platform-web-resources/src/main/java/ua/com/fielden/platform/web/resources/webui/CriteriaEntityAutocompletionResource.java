package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.IContextDecomposer.AUTOCOMPLETE_ACTIVE_ONLY_KEY;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityOrUnionType;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.*;
import static ua.com.fielden.platform.web.resources.webui.EntityAutocompletionResource.prepSearchString;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class CriteriaEntityAutocompletionResource<T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> extends AbstractWebResource {
    public static final String AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY = "@@activeOnlyChanged";
    private static final String CENTRE_DIRTY_KEY = "@@centreDirty";
    public static final String LOAD_MORE_DATA_KEY = "@@loadMoreData";
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    private final String criterionPropertyName;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<T> centre;

    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final EntityFactory entityFactory;
    private final ICentreConfigSharingModel sharingModel;

    private final Logger logger = LogManager.getLogger(getClass());

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
                    updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
                    companionFinder, critGenerator, 0L,
                    user,
                    device(),
                    webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel
                );
                criteriaType = (Class<M>) enhancedCentreEntityQueryCriteria.getType();
            } else {
                criteriaEntity = (M) createCriteriaEntityWithoutConflicts(modifHolder, companionFinder, critGenerator, miType, saveAsName, user, device(), webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
                enhancedCentreEntityQueryCriteria = criteriaEntity;
                criteriaType = (Class<M>) criteriaEntity.getType();
            }

            // TODO criteriaType is necessary to be used for 1) value matcher creation 2) providing value matcher fetch model
            // Please, investigate whether such items can be done without 'criteriaType', and this will eliminate the need to create 'criteriaEntity' (above).

            final T3<IValueMatcherWithCentreContext<T>, Optional<CentreContextConfig>, T2<String, Class<T>>> matcherAndConfigAndPropWithType;
            if (centre != null) {
                matcherAndConfigAndPropWithType = centre.<T> createValueMatcherAndContextConfig(criteriaType, criterionPropertyName);
            } else {
                final String msg = String.format("No EntityCentre instance can be found for already constructed 'criteria entity' with type [%s].", criteriaType.getName());
                logger.error(msg);
                throw new IllegalStateException(msg);
            }

            final IValueMatcherWithCentreContext<T> valueMatcher = matcherAndConfigAndPropWithType._1;
            final Optional<CentreContextConfig> contextConfig = matcherAndConfigAndPropWithType._2;
            final String origPropName = matcherAndConfigAndPropWithType._3._1;
            final Class<T> propType = matcherAndConfigAndPropWithType._3._2;

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
                eccCompanion,
                mmiCompanion,
                userCompanion,
                sharingModel
            );
            if (context.isPresent()) {
                // logger.debug("context for prop [" + criterionPropertyName + "] = " + context);
                valueMatcher.setContext(context.get());
            } else {
                valueMatcher.setContext(new CentreContext<>()); // even for empty context config, the resultant CentreContext will be present; this context may contain information about activatable autocompleter i.e. whether 'active only' (or all) values should be considered
            }

            // prepare the search string and start building custom object
            final T2<String, Integer> searchStringAndDataPageNo = prepSearchString(centreContextHolder, false);
            final Map<String, Object> customObject = linkedMapOf(t2(LOAD_MORE_DATA_KEY, searchStringAndDataPageNo._2 > 1));

            // in selection criteria autocompleter (single / multi), find out whether it is for activatable property and not explicitly hidden
            if (isActivatableEntityOrUnionType(propType) && !centre.isActiveOnlyActionHidden(origPropName)) {
                // determine data from client-side for further processing
                final Optional<Boolean> activeOnlyFromClientOpt = ofNullable((Boolean) centreContextHolder.getCustomObject().get(AUTOCOMPLETE_ACTIVE_ONLY_KEY)); // empty only for first time loading (or for non-activatables)
                final Optional<Boolean> activeOnlyChangedFromClientOpt = ofNullable((Boolean) centreContextHolder.getCustomObject().get(AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY)); // non-empty only for 'active only' button tap (always with 'true' value inside)

                // based on whether 'active only' arrived from client, apply it or not, and calculate centre dirtiness
                final Optional<Boolean> centreDirtyOpt = activeOnlyFromClientOpt.map(activeOnlyFromClient -> {
                    final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = enhancedCentreEntityQueryCriteria.adjustCentre(centreManager -> { // always apply 'activeOnly' that arrived from client; i.e. override saved value with client-side one -- no interference with possibly opened same centre configuration in other browser's tab
                        centreManager.getFirstTick().setAutocompleteActiveOnly(centre.getEntityType(), origPropName, activeOnlyFromClient);
                    });
                    return enhancedCentreEntityQueryCriteria.centreDirtyCalculator() // the centre may become dirty; need to retrieve and send this information
                        .apply(enhancedCentreEntityQueryCriteria.saveAsName())
                        .apply(() -> updatedFreshCentre); // do it efficiently without the need to retrieve fresh centre again
                });
                final boolean activeOnly = activeOnlyFromClientOpt.orElseGet(() -> enhancedCentreEntityQueryCriteria.freshCentre().getFirstTick().getAutocompleteActiveOnly(centre.getEntityType(), origPropName));

                // push 'activeOnly' into the context to be later considered by value matchers (either default or custom ones)
                valueMatcher.getContext().setCustomProperty(AUTOCOMPLETE_ACTIVE_ONLY_KEY, activeOnly);

                // return all the necessary custom data back to the client
                customObject.put(AUTOCOMPLETE_ACTIVE_ONLY_KEY, activeOnly);
                activeOnlyChangedFromClientOpt.ifPresent(activeOnlyChanged -> customObject.put(AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY, activeOnlyChanged));
                centreDirtyOpt.ifPresent(centreDirty -> customObject.put(CENTRE_DIRTY_KEY, centreDirty));
            }

            // perform value matching
            final List<? extends AbstractEntity<?>> entities =  valueMatcher.findMatchesWithModel(searchStringAndDataPageNo._1, searchStringAndDataPageNo._2);

            // logger.debug("CRITERIA_ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJsonRepresentationWithoutIdAndVersion(entities, customObject);
        }, restUtil);
    }

}