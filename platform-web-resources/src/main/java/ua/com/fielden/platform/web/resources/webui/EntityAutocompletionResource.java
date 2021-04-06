package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.MiscUtilities.prepare;
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
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityRestorationUtils;
import ua.com.fielden.platform.web.view.master.EntityMaster;

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
    private final EntityMaster<CONTEXT> master;

    public EntityAutocompletionResource(
            final Class<CONTEXT> entityType,
            final String propertyName,
            final IEntityProducer<CONTEXT> entityProducer,
            final IValueMatcherWithContext<CONTEXT, T> valueMatcher,
            final ICompanionObjectFinder companionFinder,
            final RestServerUtil restUtil,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final EntityMaster<CONTEXT> master,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.entityType = entityType;
        this.propertyName = propertyName;
        this.valueMatcher = valueMatcher;
        this.restUtil = restUtil;
        this.coFinder = companionFinder;
        this.companion = companionFinder.<IEntityDao<CONTEXT>, CONTEXT> find(this.entityType);
        this.producer = entityProducer;
        this.master = master;
    }

    /**
     * Handles POST request resulting from tg-entity-editor's (are used as editor in masters) <code>search()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            // logger.debug("ENTITY_AUTOCOMPLETION_RESOURCE: search started.");
            final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);

            final Map<String, Object> modifHolder = !centreContextHolder.proxiedPropertyNames().contains("modifHolder") ? centreContextHolder.getModifHolder() : new HashMap<>();
            final CONTEXT originallyProducedEntity = !centreContextHolder.proxiedPropertyNames().contains("originallyProducedEntity") ? (CONTEXT) centreContextHolder.getOriginallyProducedEntity() : null;
            final CONTEXT context = EntityRestorationUtils.constructEntity(modifHolder, originallyProducedEntity, companion, producer, coFinder).getKey();
            // logger.debug("context = " + context);

            final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside paramsHolder
            final String searchString = prepare(searchStringVal.contains("*") ? searchStringVal : searchStringVal + "*");
            final int dataPage = centreContextHolder.getCustomObject().containsKey("@@dataPage") ? (Integer) centreContextHolder.getCustomObject().get("@@dataPage") : 1;
            // logger.debug(format("SEARCH STRING %s, PAGE %s", searchString, dataPage));
           
            valueMatcher.setContext(context);
            final fetch<T> fetch = master.createFetchModelForAutocompleter(propertyName, (Class<T>) ("".equals(propertyName) ? entityType : determinePropertyType(entityType, propertyName)));
            valueMatcher.setFetch(fetch);
            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString != null ? searchString : "%", dataPage);

            // logger.debug("ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJsonRepresentationWithoutIdAndVersion(entities);
        }, restUtil);
    }
    
}
