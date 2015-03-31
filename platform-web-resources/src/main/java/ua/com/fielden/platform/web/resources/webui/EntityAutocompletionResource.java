package ua.com.fielden.platform.web.resources.webui;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResource<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends ServerResource {
    private final EntityResourceUtils<CONTEXT> utils;
    private final Class<CONTEXT> entityType;
    private final String propertyName;
    private final RestServerUtil restUtil;
    private final IValueMatcherWithContext<CONTEXT, T> valueMatcher;
    private final ICompanionObjectFinder coFinder;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityAutocompletionResource(
            final Class<CONTEXT> entityType,
            final String propertyName,
            final IEntityProducer<CONTEXT> entityProducer,
            final EntityFactory entityFactory,
            final IValueMatcherWithContext<CONTEXT, T> valueMatcher,
            final ICompanionObjectFinder companionFinder,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,
            final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);

        utils = new EntityResourceUtils<CONTEXT>(entityType, entityProducer, entityFactory, restUtil, companionFinder);
        this.entityType = entityType;
        this.propertyName = propertyName;
        this.valueMatcher = valueMatcher;
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        final Pair<CONTEXT, Map<String, Object>> entityAndHolder;
        if (modifiedPropertiesHolder.get("@@criteriaType") == null) {
            entityAndHolder = utils.constructEntity(modifiedPropertiesHolder);
        } else {
            entityAndHolder = constructCriteriaEntity(modifiedPropertiesHolder);
            modifiedPropertiesHolder.remove("@@criteriaType");
        }
        final CONTEXT context = entityAndHolder.getKey();
        logger.debug("context = " + context);
        final Map<String, Object> paramsHolder = entityAndHolder.getValue();

        final String searchStringVal = (String) paramsHolder.get("@@searchString"); // custom property inside paramsHolder
        logger.debug(String.format("SEARCH STRING %s", searchStringVal));

        final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
        logger.debug(String.format("SEARCH STRING %s", searchString));

        valueMatcher.setContext(context);
        final fetch<T> fetch = EntityResourceUtils.<CONTEXT, T> fetchForProperty(coFinder, entityType, propertyName).fetchModel();
        valueMatcher.setFetch(fetch);
        final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

        return restUtil.listJSONRepresentation(entities);
    }

    private Pair<CONTEXT, Map<String, Object>> constructCriteriaEntity(final Map<String, Object> modifiedPropertiesHolder) {
        final Class<? extends AbstractEntity<?>> criteriaType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass((String) modifiedPropertiesHolder.get("@@criteriaType"));
        final Class<? extends MiWithConfigurationSupport<?>> miType = CriteriaResource.getMiType(criteriaType);
        final CONTEXT valPrototype = (CONTEXT) CriteriaResource.createCriteriaValidationPrototype(miType, CriteriaResource.getCurrentCentreManager(gdtm, miType), critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        return EntityResourceUtils.constructEntity(modifiedPropertiesHolder, valPrototype, coFinder);
    }
}
