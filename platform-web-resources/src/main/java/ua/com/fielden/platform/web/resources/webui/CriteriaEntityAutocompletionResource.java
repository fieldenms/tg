package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.utils.MiscUtilities.prepare;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCentreContext;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityWithoutConflicts;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaValidationPrototype;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;

import java.util.HashMap;
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
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
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
            final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);
            
            final M criteriaEntity;
            final Class<M> criteriaType;
            final Map<String, Object> modifHolder = !centreContextHolder.proxiedPropertyNames().contains("modifHolder") ? centreContextHolder.getModifHolder() : new HashMap<>();
            if (CentreResourceUtils.isEmpty(modifHolder)) {
                // this branch is used for criteria entity generation to get the type of that entity later -- the modifiedPropsHolder is empty (no 'selection criteria' is needed in the context).
                criteriaEntity = null;
                final M enhancedCentreEntityQueryCriteria = createCriteriaValidationPrototype(
                    miType, saveAsName,
                    updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder),
                    companionFinder, critGenerator, 0L, 
                    user, userProvider,
                    device(),
                    domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion
                );
                criteriaType = (Class<M>) enhancedCentreEntityQueryCriteria.getClass();
            } else {
                criteriaEntity = (M) createCriteriaEntityWithoutConflicts(modifHolder, companionFinder, critGenerator, miType, saveAsName, user, userProvider, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
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
                userProvider,
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
                userCompanion
            );
            if (context.isPresent()) {
                // logger.debug("context for prop [" + criterionPropertyName + "] = " + context);
                valueMatcher.setContext(context.get());
            } else {
                // TODO check whether such setting is needed (need to test autocompletion in centres without that setting) or can be removed:
                valueMatcher.setContext(new CentreContext<>());
            }

            // prepare search string
            final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside customObject
            final String searchString = prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
            final int dataPage = centreContextHolder.getCustomObject().containsKey("@@dataPage") ? (Integer) centreContextHolder.getCustomObject().get("@@dataPage") : 1;
            // logger.debug(format("SEARCH STRING %s, PAGE %s", searchString, dataPage));
            
            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%", dataPage);

            // logger.debug("CRITERIA_ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJsonRepresentationWithoutIdAndVersion(entities);
        }, restUtil);
    }

}