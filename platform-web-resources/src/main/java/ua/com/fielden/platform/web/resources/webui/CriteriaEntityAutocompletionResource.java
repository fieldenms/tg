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
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class CriteriaEntityAutocompletionResource<CRITERIA extends AbstractEntity<?>, T extends AbstractEntity<?>> extends ServerResource {
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final String criterionPropertyName;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder coFinder;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<T> centre;
    private final Logger logger = Logger.getLogger(getClass());

    public CriteriaEntityAutocompletionResource(
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final String criterionPropertyName,
            final EntityCentre<T> centre,
            final ICompanionObjectFinder companionFinder,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,
            final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);

        this.miType = miType;
        this.criterionPropertyName = criterionPropertyName;
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;
        this.centre = centre;
    }

    /**
     * Handles POST request resulting from tg-entity-search-criteria's / tg-entity-editor's (both they are used as criteria editors in centres) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

        final Pair<CRITERIA, Map<String, Object>> entityAndHolder = constructCriteriaEntity(centreContextHolder.getModifHolder());

        // TODO criteriaType is necessary to be used for 1) value matcher creation 2) providing value matcher fetch model
        // Please, investigate whether such items can be done without 'criteriaType', and this will eliminate the need to create 'criteriaEntity' (above).
        final Class<CRITERIA> criteriaType = (Class<CRITERIA>) entityAndHolder.getKey().getClass();

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
        final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside customObject
        final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
        logger.debug(String.format("SEARCH STRING %s", searchString));

        final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

        return restUtil.listJSONRepresentation(entities);
    }

    /**
     * TODO this method needs to be unified with other 'constructCriteriaEntity' methods. The unification process has been started, but not finished.
     * Ideally there should be single version of this method throughout the application!
     *
     * @param modifiedPropertiesHolder
     * @return
     */
    private Pair<CRITERIA, Map<String, Object>> constructCriteriaEntity(final Map<String, Object> modifiedPropertiesHolder) {
        if (!modifiedPropertiesHolder.containsKey(AbstractEntity.VERSION)) {
            // this branch is used for criteria entity generation to get the type of that entity later -- the modifiedPropsHolder is empty (no 'selection criteria' is needed in the context).
            final CRITERIA valPrototype = (CRITERIA) CentreResourceUtils.createCriteriaValidationPrototype(miType, CentreResourceUtils.getFreshCentre(gdtm, miType), critGenerator, 0L /* TODO does it matter? */);
            return new Pair<>(
                    valPrototype,
                    modifiedPropertiesHolder//
            );
        } else {
            // [This is the old version, not consistent with other 'constructCriteriaEntity' methods!] final CRITERIA valPrototype = (CRITERIA) CentreResourceUtils.createCriteriaValidationPrototype(miType, CentreResourceUtils.getFreshCentre(gdtm, miType), critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
            // return EntityResourceUtils.constructEntity(modifiedPropertiesHolder, valPrototype, coFinder);
            final ICentreDomainTreeManagerAndEnhancer originalCdtmae = CentreResourceUtils.getFreshCentre(gdtm, miType);

            CentreResourceUtils.applyMetaValues(originalCdtmae, CentreResourceUtils.getEntityType(miType), modifiedPropertiesHolder);
            final CRITERIA valPrototype = (CRITERIA) CentreResourceUtils.createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));

            final Pair<CRITERIA, Map<String, Object>> entityAndHolder = (Pair<CRITERIA, Map<String, Object>>)
            EntityResourceUtils.constructCriteriaEntityAndResetMetaValues(
                    modifiedPropertiesHolder,
                    (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) valPrototype,
                    CentreUtils.getOriginalManagedType(valPrototype.getType(), originalCdtmae),
                    coFinder//
            );

            return entityAndHolder;
        }
    }
}
