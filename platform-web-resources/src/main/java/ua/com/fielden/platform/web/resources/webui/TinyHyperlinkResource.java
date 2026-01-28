package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.jackson.EntitySerialiser;
import ua.com.fielden.platform.serialisation.jackson.PropertyDeserialisationErrorHandler;
import ua.com.fielden.platform.tiny.TinyHyperlink;
import ua.com.fielden.platform.tiny.TinyHyperlinkCo;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyAssignmentErrorHandler;

import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.tiny.TinyHyperlink.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.restoreEntityFrom;
import static ua.com.fielden.platform.web.resources.webui.MultiActionUtils.createPropertyActionIndicesForMaster;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

public class TinyHyperlinkResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger();

    private final RestServerUtil restUtil;

    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final EntityFactory factory;
    private final ICentreConfigSharingModel sharingModel;
    private final String requestHash;
    private final ISerialiser serialiser;

    public TinyHyperlinkResource(
            final EntityFactory entityFactory,
            final RestServerUtil restUtil,
            final ICriteriaGenerator critGenerator,
            final ICompanionObjectFinder companionFinder,
            final ISerialiser serialiser,

            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICentreConfigSharingModel sharingModel,

            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);

        this.companionFinder = companionFinder;
        this.serialiser = serialiser;
        this.critGenerator = critGenerator;
        this.restUtil = restUtil;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.factory = entityFactory;
        this.sharingModel = sharingModel;
        this.requestHash = Objects.toString(request.getAttributes().get(HASH), null);
        if (requestHash == null || requestHash.isBlank()) {
            throw new InvalidStateException("[requestHash] must be present.");
        }
    }

    /// Handles a GET request to open a [TinyHyperlink].
    ///
    @Get
    public Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> {
            final TinyHyperlinkCo coTinyHyperlink = companionFinder.findAsReader(TinyHyperlink.class, true);
            final var tinyHyperlink = coTinyHyperlink.findByKeyAndFetch(fetchIdOnly(TinyHyperlink.class).with(ENTITY_TYPE_NAME, HASH, SAVING_INFO_HOLDER, ACTION_IDENTIFIER, TARGET), requestHash);
            if (tinyHyperlink == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return restUtil.errorJsonRepresentation("The resource you're looking for can't be found.\nPlease check the link or contact support if you need assistance.");
            }

            // If this tiny hyperlink points to another URL.
            if (tinyHyperlink.getTarget() != null) {
                final Map<String, Object> customObject = linkedMapOf(t2(CUSTOM_OBJECT_SHARED_URI, tinyHyperlink.getTarget().value));
                return restUtil.resultJSONRepresentation(successful().extendResultWithCustomObject(customObject));
            }
            // Otherwise, it represents a shared entity.

            final PropertyDeserialisationErrorHandler propDeserialisationErrorHandler = (entity, property, inputValueSupplier, error) -> {
                LOGGER.warn(() -> format("[tiny/%s] Suppressed the following error during deserialisation: %s",
                                         tinyHyperlink.getHash(),
                                         PropertyDeserialisationErrorHandler.makeMessage(entity, property, inputValueSupplier)),
                            error);
                // Ignore non-existing properties.
                // Assign a warning if property deserialisation fails.
                if (isPropertyPresent(entity.getType(), property)) {
                    entity.getPropertyOptionally(property).ifPresent(mp -> mp.setDomainValidationResult(warning("The configured value could not be used.")));
                }
            };

            final PropertyAssignmentErrorHandler propApplicationErrorHandler = (entity, property, value, error) -> {
                LOGGER.warn(() -> format("[tiny/%s] Suppressed the following error during property application: %s",
                                         tinyHyperlink.getHash(),
                                         PropertyAssignmentErrorHandler.makeMessage(entity, property, value)),
                            error);
                // Ignore non-existing properties.
                // Assign a warning if property application fails.
                if (isPropertyPresent(entity.getType(), property)) {
                    // The meta-property should exist, but let's be defensive.
                    entity.getPropertyOptionally(property).ifPresent(mp -> mp.setDomainValidationResult(warning("The configured value could not be used.")));
                }
            };

            final SavingInfoHolder savingInfoHolder;
            EntitySerialiser.getContext().setPropDeserialisationErrorHandler(propDeserialisationErrorHandler);
            try {
                savingInfoHolder = serialiser.deserialise(tinyHyperlink.getSavingInfoHolder(), SavingInfoHolder.class, SerialiserEngines.JACKSON);
            } finally {
                EntitySerialiser.getContext().removePropDeserialisationErrorHandler();
            }

            final var entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(tinyHyperlink.getEntityTypeName());
            final var entity = restoreEntityFrom(true,
                                                 savingInfoHolder,
                                                 entityType,
                                                 propApplicationErrorHandler,
                                                 factory,
                                                 webUiConfig,
                                                 companionFinder,
                                                 userProvider.getUser(),
                                                 critGenerator,
                                                 0,
                                                 device(),
                                                 sharingModel);

            final Map<String, Object> customObject = linkedMapOf(createPropertyActionIndicesForMaster(entity, webUiConfig));
            customObject.put(CUSTOM_OBJECT_ACTION_IDENTIFIER, tinyHyperlink.getActionIdentifier());
            // Send the deserialised `savingInfoHolder` to the client instead of the original `tinyHyperlink.savingInfoHolder`.
            // Since we skip properties that could not be deserialised (via a custom error handler), the deserialised form will be free of erroneous elements.
            // If we sent the original form, the client could use it for subsequent requests to other resources that do not use a lenient error handler for deserialisation,
            // ultimately causing unexpected errors.
            customObject.put(SAVING_INFO_HOLDER, new String(serialiser.serialise(savingInfoHolder, SerialiserEngines.JACKSON)));
            return restUtil.resultJSONRepresentation(restUtil.singleEntityResult(entity).extendResultWithCustomObject(customObject));
        }, restUtil);
    }

}
