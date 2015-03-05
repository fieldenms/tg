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
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResource<PARENT_TYPE extends AbstractEntity<?>> extends ServerResource {
    private final EntityResourceUtils<PARENT_TYPE> utils;
    private final Class<PARENT_TYPE> entityType;
    private final String propertyName;
    private final RestServerUtil restUtil;
    private final IValueMatcherWithContext<PARENT_TYPE, ? extends AbstractEntity<?>> valueMatcher;
    private final IFetchProvider<PARENT_TYPE> fetchProvider;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityAutocompletionResource(
            final Class<PARENT_TYPE> entityType,
            final String propertyName,
            final IEntityProducer<PARENT_TYPE> entityProducer,
            final EntityFactory entityFactory,
            final IValueMatcherWithContext<PARENT_TYPE, ? extends AbstractEntity<?>> valueMatcher,
            final ICompanionObjectFinder companionFinder,
            final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);

        utils = new EntityResourceUtils<PARENT_TYPE>(entityType, entityProducer, entityFactory, restUtil, companionFinder);
        this.entityType = entityType;
        this.propertyName = propertyName;
        this.valueMatcher = valueMatcher;
        this.fetchProvider = companionFinder.find(this.entityType).getFetchProvider();
        this.restUtil = restUtil;
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final Pair<PARENT_TYPE, Map<String, Object>> entityAndHolder = utils.constructEntity(envelope, restUtil);
        final PARENT_TYPE context = entityAndHolder.getKey();
        logger.error("context = " + context);
        final Map<String, Object> paramsHolder = entityAndHolder.getValue();

        final String searchStringVal = (String) paramsHolder.get("@searchString"); // custom property inside paramsHolder
        logger.debug(String.format("SEARCH STRING %s", searchStringVal));

        final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
        logger.debug(String.format("SEARCH STRING %s", searchString));

        valueMatcher.setContext(context);
        valueMatcher.setFetchModel(fetchProvider.fetchFor(propertyName).fetchModel());
        final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

        return restUtil.listJSONRepresentation(entities);
    }
}
