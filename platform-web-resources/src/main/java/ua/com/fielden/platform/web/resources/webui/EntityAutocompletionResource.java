package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;
import ua.com.fielden.platform.web.utils.EntityRestorationUtils;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResource<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractWebResource {
    private static final Logger logger = Logger.getLogger(EntityAutocompletionResource.class);
    private final Class<CONTEXT> entityType;
    private final String propertyName;
    private final RestServerUtil restUtil;
    private final IValueMatcherWithContext<CONTEXT, T> valueMatcher;
    private final ICompanionObjectFinder coFinder;
    private final IEntityDao<CONTEXT> companion;
    private final IEntityProducer<CONTEXT> producer;

    public EntityAutocompletionResource(
            final Class<CONTEXT> entityType,
            final String propertyName,
            final IEntityProducer<CONTEXT> entityProducer,
            final IValueMatcherWithContext<CONTEXT, T> valueMatcher,
            final ICompanionObjectFinder companionFinder,
            final RestServerUtil restUtil,
            final IDeviceProvider deviceProvider,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider);

        this.entityType = entityType;
        this.propertyName = propertyName;
        this.valueMatcher = valueMatcher;
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
        this.companion = companionFinder.<IEntityDao<CONTEXT>, CONTEXT> find(this.entityType);
        this.producer = entityProducer;
    }

    /**
     * Handles POST request resulting from tg-entity-editor's (are used as editor in masters) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("ENTITY_AUTOCOMPLETION_RESOURCE: search started.");
            final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);

            final Map<String, Object> modifHolder = !centreContextHolder.proxiedPropertyNames().contains("modifHolder") ? centreContextHolder.getModifHolder() : new HashMap<String, Object>();
            final CONTEXT originallyProducedEntity = !centreContextHolder.proxiedPropertyNames().contains("originallyProducedEntity") ? (CONTEXT) centreContextHolder.getOriginallyProducedEntity() : null;
            final CONTEXT context = EntityRestorationUtils.constructEntity(modifHolder, originallyProducedEntity, companion, producer, coFinder).getKey();
            logger.debug("context = " + context);

            final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside paramsHolder
            logger.debug(String.format("SEARCH STRING %s", searchStringVal));

            final String searchString = PojoValueMatcher.prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
            logger.debug(String.format("SEARCH STRING %s", searchString));

            valueMatcher.setContext(context);
            final fetch<T> fetch = EntityResourceUtils.<CONTEXT, T> fetchForProperty(coFinder, entityType, propertyName).fetchModel();
            valueMatcher.setFetch(fetch);
            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%");

            logger.debug("ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJSONRepresentation(entities);
        }, restUtil);
    }
}
