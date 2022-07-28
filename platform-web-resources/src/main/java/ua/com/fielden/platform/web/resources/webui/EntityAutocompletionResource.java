package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;
import static ua.com.fielden.platform.utils.MiscUtilities.prepare;
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

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
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

            valueMatcher.setContext(context);
            final Class<T> propType = (Class<T>) ("".equals(propertyName) ? entityType : determinePropertyType(entityType, propertyName));
            if (!isPropertyDescriptor(propType)) {
                valueMatcher.setFetch(master.createFetchModelForAutocompleter(propertyName, propType));
            }

            // The search string values used to be upper cased at the client side in tg-entity-editor.js.
            // However, this is not suitable for values of type Attachment, where new hyperlink instances are created ad hoc for autocompletion and, if chosen, such values distort the typed in or copy/pasted URIs.
            // This is why upper casing of the search strings was removed in tg-entity-editor.js and introduces in this web resource for ease of maintenance and customisation.
            // There could be other cases where upper casing should not be applied, but for now the change is limited to entity Attachment only.
            final boolean shouldUpperCase;
            if (Attachment.class.isAssignableFrom(propType)) {
                shouldUpperCase = false;
            } else {
                shouldUpperCase = true;
            }

            final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside paramsHolder
            final Optional<String> prepSearchString = ofNullable(prepare(searchStringVal.contains("*") || searchStringVal.contains("%") ? searchStringVal : searchStringVal + "*"));
            final String searchString = prepSearchString.map(str -> shouldUpperCase ? str.toUpperCase() : str).orElse("%");
            final int dataPage = centreContextHolder.getCustomObject().containsKey("@@dataPage") ? (Integer) centreContextHolder.getCustomObject().get("@@dataPage") : 1;
            // logger.debug(format("SEARCH STRING %s, PAGE %s", searchString, dataPage));
            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchString, dataPage);

            // logger.debug("ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJsonRepresentationWithoutIdAndVersion(entities);
        }, restUtil);
    }

}
