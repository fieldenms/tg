package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyAssignmentErrorHandler;
import ua.com.fielden.platform.web.utils.EntityRestorationUtils;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.IContextDecomposer.AUTOCOMPLETE_ACTIVE_ONLY_KEY;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityOrUnionType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;
import static ua.com.fielden.platform.utils.MiscUtilities.prepare;
import static ua.com.fielden.platform.web.resources.webui.CriteriaEntityAutocompletionResource.AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY;
import static ua.com.fielden.platform.web.resources.webui.CriteriaEntityAutocompletionResource.LOAD_MORE_DATA_KEY;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;

/**
 * The web resource for entity autocompletion serves as a back-end mechanism of searching entities by search strings and using additional parameters.
 *
 * @author TG Team
 *
 */
public class EntityAutocompletionResource<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractWebResource {
    private static final Logger logger = LogManager.getLogger(EntityAutocompletionResource.class);
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
            final CONTEXT context = EntityRestorationUtils.constructEntity(modifHolder, PropertyAssignmentErrorHandler.standard, originallyProducedEntity, companion, producer, coFinder).getKey();
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
            final boolean shouldUpperCase = !Attachment.class.isAssignableFrom(propType);
            // prepare the search string and perform value matching
            final T2<String, Integer> searchStringAndDataPageNo = prepSearchString(centreContextHolder, shouldUpperCase);
            final Map<String, Object> customObject = linkedMapOf(t2(LOAD_MORE_DATA_KEY, searchStringAndDataPageNo._2 > 1));

            // For a master autocompleter, we need to determine if it is for an activatable property and can match inactive values.
            // If so, we need to show the "exclude inactive values" action.
            if (isActivatableEntityOrUnionType(propType) && valueMatcher instanceof FallbackValueMatcherWithContext<?, ?> matcher) {
                if (!matcher.activeOnlyByDefault) { // Match inactive values?
                    // Read the client-side user configuration for an autocompleter.
                    // AUTOCOMPLETE_ACTIVE_ONLY_KEY is empty only when loading the data for the first time.
                    final var activeOnlyFromClientOpt = ofNullable((Boolean) centreContextHolder.getCustomObject().get(AUTOCOMPLETE_ACTIVE_ONLY_KEY));
                    // AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY is present only if the current request is the result of user tapping "exclude inactive values" button, and its value is always `true`.
                    final var activeOnlyChangedFromClientOpt = ofNullable((Boolean) centreContextHolder.getCustomObject().get(AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY));

                    final boolean activeOnly = activeOnlyFromClientOpt.orElse(false);
                    matcher.setActiveOnly(activeOnly);

                    // Return the autocompleter configuration back to the client.
                    customObject.put(AUTOCOMPLETE_ACTIVE_ONLY_KEY, activeOnly);
                    activeOnlyChangedFromClientOpt.ifPresent(activeOnlyChanged -> customObject.put(AUTOCOMPLETE_ACTIVE_ONLY_CHANGED_KEY, true));
                }
            }

            final List<? extends AbstractEntity<?>> entities = valueMatcher.findMatchesWithModel(searchStringAndDataPageNo._1, searchStringAndDataPageNo._2);

            // logger.debug("ENTITY_AUTOCOMPLETION_RESOURCE: search finished.");
            return restUtil.listJsonRepresentationWithoutIdAndVersion(entities, customObject);
        }, restUtil);
    }

    /**
     * Obtains a search string from {@code centreContextHolder} and prepares it to be used for finding the matching values.
     *
     * @param centreContextHolder an centre context holder.
     * @param shouldUpperCase indicates whether the search string should be in uppercase.
     * @return a search string and a data page number to be retrieved to support loading of "more" data
     */
    public static T2<String, Integer> prepSearchString(final CentreContextHolder centreContextHolder, final boolean shouldUpperCase) {
        final String searchStringVal = (String) centreContextHolder.getCustomObject().get("@@searchString"); // custom property inside paramsHolder
        final Optional<String> maybeSearchString = ofNullable(prepare(searchStringVal.contains("*") || searchStringVal.contains("%") ? searchStringVal : "*" + searchStringVal + "*"));
        final String searchString = maybeSearchString.map(str -> shouldUpperCase ? str.toUpperCase() : str).orElse("%");
        final Optional<Integer> maybeDataPage = ofNullable((Integer) centreContextHolder.getCustomObject().get("@@dataPage"));
        final int dataPageNo =  maybeDataPage.orElse(1);
        return t2(searchString, dataPageNo);
    }

}
