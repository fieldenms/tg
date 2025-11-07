package ua.com.fielden.platform.web.resources.webui;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.tiny.TinyHyperlink;
import ua.com.fielden.platform.tiny.TinyHyperlinkCo;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyApplicationErrorHandler;

import java.util.Map;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.tiny.TinyHyperlink.*;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.restoreEntityFrom;
import static ua.com.fielden.platform.web.resources.webui.MultiActionUtils.createPropertyActionIndicesForMaster;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

public class TinyHyperlinkResource extends AbstractWebResource {

    private final RestServerUtil restUtil;

    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final EntityFactory factory;
    private final ICentreConfigSharingModel sharingModel;
    private final Long tinyHyperlinkId;
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
            final Response response,

            final Long tinyHyperlinkId)
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
        this.tinyHyperlinkId = tinyHyperlinkId;
    }

    /// Handles a GET request to open a [TinyHyperlink].
    ///
    @Get
    public Representation save() {
        return handleUndesiredExceptions(getResponse(), () -> {
            final TinyHyperlinkCo coTinyHyperlink = companionFinder.findAsReader(TinyHyperlink.class, true);
            final var tinyHyperlink = coTinyHyperlink.findById(tinyHyperlinkId, fetchIdOnly(TinyHyperlink.class).with(ENTITY_TYPE_NAME, SAVING_INFO_HOLDER, ACTION_IDENTIFIER));
            if (tinyHyperlink == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new StringRepresentation("The specified resource was not found. Please verify that you are accessing the correct resource.");
            }
            final var entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(tinyHyperlink.getEntityTypeName());
            final var savingInfoHolder = serialiser.deserialise(tinyHyperlink.getSavingInfoHolder(), SavingInfoHolder.class, SerialiserEngines.JACKSON);
            final var entity = restoreEntityFrom(true,
                                                 savingInfoHolder,
                                                 entityType,
                                                 PropertyApplicationErrorHandler.logging.and(propertyApplicationErrorHandler),
                                                 factory,
                                                 webUiConfig,
                                                 companionFinder,
                                                 userProvider.getUser(),
                                                 critGenerator,
                                                 0,
                                                 device(),
                                                 companionFinder.find(EntityCentreConfig.class),
                                                 companionFinder.find(MainMenuItem.class),
                                                 companionFinder.find(User.class),
                                                 sharingModel);

            final Map<String, Object> customObject = linkedMapOf(createPropertyActionIndicesForMaster(entity, webUiConfig));
            customObject.put(ACTION_IDENTIFIER, tinyHyperlink.getActionIdentifier());
            customObject.put(SAVING_INFO_HOLDER, new String(tinyHyperlink.getSavingInfoHolder()));
            return restUtil.resultJSONRepresentation(restUtil.singleEntityResult(entity).extendResultWithCustomObject(customObject));
        }, restUtil);
    }

    private static final PropertyApplicationErrorHandler propertyApplicationErrorHandler = (entity, property, _, _) -> {
        // Ignore non-existing properties.
        // Assign a warning if property application fails.
        if (isPropertyPresent(entity.getType(), property)) {
            // The meta-property should exist, but let's be defensive.
            entity.getPropertyOptionally(property).ifPresent(mp -> mp.setDomainValidationResult(warning("The configured value could not be used.")));
        }
    };

}
