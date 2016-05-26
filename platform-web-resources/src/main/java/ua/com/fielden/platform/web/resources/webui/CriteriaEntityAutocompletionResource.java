package ua.com.fielden.platform.web.resources.webui;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUpdater;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class CriteriaEntityAutocompletionResource<T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> extends ServerResource {
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final String criterionPropertyName;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder coFinder;
    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<T> centre;
    
    private final IWebUiConfig webUiConfig; 
    private final IServerGlobalDomainTreeManager serverGdtm; 
    private final IUserProvider userProvider;
    private final EntityFactory entityFactory; 
    
    private final Logger logger = Logger.getLogger(getClass());

    public CriteriaEntityAutocompletionResource(
            final IWebUiConfig webUiConfig, 
            final ICompanionObjectFinder companionFinder, 
            final IServerGlobalDomainTreeManager serverGdtm, 
            final IUserProvider userProvider, 
            final ICriteriaGenerator critGenerator, 
            final EntityFactory entityFactory, 
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String criterionPropertyName,
            final EntityCentre<T> centre,
            final RestServerUtil restUtil, 
            final Context context, 
            final Request request, 
            final Response response) {
        init(context, request, response);

        this.miType = miType;
        this.criterionPropertyName = criterionPropertyName;
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
        this.critGenerator = critGenerator;
        this.centre = centre;
        
        this.webUiConfig = webUiConfig; 
        this.serverGdtm = serverGdtm; 
        this.userProvider = userProvider;
        this.entityFactory = entityFactory; 
    }

    /**
     * Handles POST request resulting from tg-entity-search-criteria's / tg-entity-editor's (both they are used as criteria editors in centres) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("CRITERIA_ENTITY_AUTOCOMPLETION_RESOURCE: search started.");
            //            // NOTE: the following line can be the example how 'entity search' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during criteria entity searching.");
            final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

            final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);

            final M criteriaEntity;
            final Class<M> criteriaType;
            if (CentreResourceUtils.isEmpty(centreContextHolder.getModifHolder())) {
                // this branch is used for criteria entity generation to get the type of that entity later -- the modifiedPropsHolder is empty (no 'selection criteria' is needed in the context).
                criteriaEntity = null;
                final M enhancedCentreEntityQueryCriteria = CentreResourceUtils.createCriteriaValidationPrototype(miType, CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME), critGenerator, 0L, gdtm);
                criteriaType = (Class<M>) enhancedCentreEntityQueryCriteria.getClass();

            } else {
                criteriaEntity = (M) CentreResourceUtils.createCriteriaEntity(centreContextHolder.getModifHolder(), coFinder, critGenerator, miType, gdtm);
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
            final Optional<CentreContext<T, ?>> context = CentreResourceUtils.createCentreContext(
                    webUiConfig, 
                    coFinder, 
                    serverGdtm, 
                    userProvider, 
                    critGenerator, 
                    entityFactory, 
                    centreContextHolder, 
                    criteriaEntity, 
                    contextConfig);
            if (context.isPresent()) {
                logger.debug("context for prop [" + criterionPropertyName + "] = " + context);
                valueMatcher.setContext(context.get());
            } else {
                // TODO check whether such setting is needed (need to test autocompletion in centres without that setting) or can be removed:
                valueMatcher.setContext(new CentreContext<>());
            }

            // populate fetch model
            valueMatcher.setFetch(EntityResourceUtils.<M, T> fetchForProperty(coFinder, criteriaType, criterionPropertyName).fetchModel());

            // prepare search string
            final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside customObject
            final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
            logger.debug(String.format("SEARCH STRING %s", searchString));

            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

            logger.debug("CRITERIA_ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJSONRepresentation(entities);
        }, restUtil);
    }
}
