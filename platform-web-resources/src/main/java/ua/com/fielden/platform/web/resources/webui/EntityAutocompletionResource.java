package ua.com.fielden.platform.web.resources.webui;

import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
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
    private final Logger logger = Logger.getLogger(getClass());

    public EntityAutocompletionResource(
            final Class<CONTEXT> entityType,
            final String propertyName,
            final IEntityProducer<CONTEXT> entityProducer,
            final EntityFactory entityFactory,
            final IValueMatcherWithContext<CONTEXT, T> valueMatcher,
            final ICompanionObjectFinder companionFinder,
            final RestServerUtil restUtil,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        utils = new EntityResourceUtils<CONTEXT>(entityType, entityProducer, entityFactory, companionFinder);
        this.entityType = entityType;
        this.propertyName = propertyName;
        this.valueMatcher = valueMatcher;
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
    }

    /**
     * Handles POST request resulting from tg-entity-editor's (are used as editor in masters) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            //            // NOTE: the following line can be the example how 'entity search' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during entity searching.");
            final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

            final CONTEXT context = utils.constructEntity(centreContextHolder.getModifHolder()).getKey();
            logger.debug("context = " + context);

            final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside paramsHolder
            logger.debug(String.format("SEARCH STRING %s", searchStringVal));

            final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
            logger.debug(String.format("SEARCH STRING %s", searchString));

            valueMatcher.setContext(context);
            final fetch<T> fetch = EntityResourceUtils.<CONTEXT, T> fetchForProperty(coFinder, entityType, propertyName).fetchModel();
            valueMatcher.setFetch(fetch);
            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

            return restUtil.listJSONRepresentation(entities);
        }, restUtil);
    }
}
