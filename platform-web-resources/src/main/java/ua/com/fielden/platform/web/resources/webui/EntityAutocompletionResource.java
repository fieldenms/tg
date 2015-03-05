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
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResource extends ServerResource {
    private final Class<? extends AbstractEntity<?>> entityType;
    private final String propertyName;
    private final RestServerUtil restUtil;
    private final IValueMatcherWithContext<?, ?> valueMatcher; // should be IContextValueMatcher
    private final IFetchProvider<?> fetchProvider;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityAutocompletionResource(
                    final Class<? extends AbstractEntity<?>> entityType,
                    final String propertyName,
                    final IValueMatcherWithContext<?, ?> valueMatcher,
                    final ICompanionObjectFinder companionFinder,
                    final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);

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
        final Map<String, Object> paramsHolder = restoreParamsHolderFrom(envelope, restUtil);

        final String searchStringVal = (String) paramsHolder.get("___searchString");
        logger.debug(String.format("SEARCH STRING %s", searchStringVal));

        final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
        logger.debug(String.format("SEARCH STRING %s", searchString));
        // TODO valueMatcher.setContext <- from paramsHolder

        valueMatcher.setFetchModel(fetchProvider.fetchFor(propertyName).fetchModel());
        final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString !=null ? searchString : "%");

        return restUtil.listJSONRepresentation(entities);
    }

    /**
     * Restores the holder of parameters into the map [paramName; paramValue].
     *
     * @param envelope
     * @return
     */
    public Map<String, Object> restoreParamsHolderFrom(final Representation envelope, final RestServerUtil restUtil) {
        try {
            return (Map<String, Object>) restUtil.restoreJSONMap(envelope);
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during deserialisation of modified properties holder, which should be validated.", ex);
            throw new IllegalStateException(ex);
        }
    }

}
