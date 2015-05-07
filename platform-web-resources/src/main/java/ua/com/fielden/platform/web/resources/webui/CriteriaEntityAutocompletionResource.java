package ua.com.fielden.platform.web.resources.webui;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class CriteriaEntityAutocompletionResource<CRITERIA extends AbstractEntity<?>, T extends AbstractEntity<?>> extends ServerResource {
    private final Class<CRITERIA> criteriaType;
    private final String criterionPropertyName;
    private final RestServerUtil restUtil;
    private final IValueMatcherWithCentreContext<T> valueMatcher;
    private final Optional<CentreContextConfig> contextConfig;
    private final ICompanionObjectFinder coFinder;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;
    private final Logger logger = Logger.getLogger(getClass());

    public CriteriaEntityAutocompletionResource(
            final Class<CRITERIA> criteriaType,
            final String criterionPropertyName,
            final Pair<IValueMatcherWithCentreContext<T>, Optional<CentreContextConfig>> valueMatcherAndContextConfig,
            final ICompanionObjectFinder companionFinder,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,
            final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);

        this.criteriaType = criteriaType;
        this.criterionPropertyName = criterionPropertyName;
        this.valueMatcher = valueMatcherAndContextConfig.getKey();
        this.contextConfig = valueMatcherAndContextConfig.getValue();
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;
    }

    /**
     * Handles POST request resulting from tg-entity-search-criteria's / tg-entity-editor's (both they are used as criteria editors in centres) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

        final Pair<CRITERIA, Map<String, Object>> entityAndHolder = constructCriteriaEntity(centreContextHolder.getModifHolder());

        // create context, if any
        final Optional<CentreContext<T, AbstractEntity<?>>> context = CentreResourceUtils.createCentreContext(centreContextHolder, (EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>) entityAndHolder.getKey(), contextConfig);
        if (context.isPresent()) {
            logger.debug("context for prop [" + criterionPropertyName + "] = " + context);
            valueMatcher.setContext(context.get());
        } else {
            // TODO check whether such setting is needed (need to test autocompletion in centres without that setting) or can be removed:
            valueMatcher.setContext(new CentreContext<>());
        }

        // populate fetch model
        valueMatcher.setFetch(EntityResourceUtils.<CRITERIA, T> fetchForProperty(coFinder, criteriaType, criterionPropertyName).fetchModel());

        // prepare search string
        final String searchStringVal = (String) entityAndHolder.getValue().get("@@searchString"); // custom property inside paramsHolder
        final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
        logger.debug(String.format("SEARCH STRING %s", searchString));

        final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

        return restUtil.listJSONRepresentation(entities);
    }

    private Pair<CRITERIA, Map<String, Object>> constructCriteriaEntity(final Map<String, Object> modifiedPropertiesHolder) {
        final Class<? extends MiWithConfigurationSupport<?>> miType = CentreUtils.getMiType(criteriaType);
        final CRITERIA valPrototype = (CRITERIA) CentreResourceUtils.createCriteriaValidationPrototype(miType, CentreResourceUtils.getFreshCentre(gdtm, miType), critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        return EntityResourceUtils.constructEntity(modifiedPropertiesHolder, valPrototype, coFinder);
    }
}
