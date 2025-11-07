package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.boot.archive.scan.spi.PackageInfoArchiveEntryHandler;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.tiny.EntityShareActionProducer;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils.PropertyApplicationErrorHandler;
import ua.com.fielden.platform.web.utils.EntityRestorationUtils;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import java.util.*;

import static java.util.Optional.*;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.web.centre.CentreContext.INSTANCEBASEDCONTINUATION_PROPERTY_NAME;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.valueOf;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCentreContext;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityForContext;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.*;
import static ua.com.fielden.platform.web.resources.webui.MultiActionUtils.createPropertyActionIndicesForMaster;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.tabs;
import static ua.com.fielden.platform.web.utils.EntityRestorationUtils.createValidationPrototype;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.*;

/// The web resource for entity serves as a back-end mechanism of entity retrieval, saving and deletion.
/// It provides a base implementation for handling the following methods:
///
/// - retrieve entity — GET request;
/// - save new entity — PUT request with an envelope containing an instance of an entity to be persisted;
/// - save already persisted entity — POST request with an envelope containing an instance of an modified entity to be changed;
/// - delete entity — DELETE request.
///
public class EntityResource<T extends AbstractEntity<?>> extends AbstractWebResource {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ERR_ENTITY_DELETION = "The entity with id [%s] and type [%s] can not be deleted due to existing dependencies.";

    private final RestServerUtil restUtil;
    private final Long entityId;
    private final EntityIdKind entityIdKind;

    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final Class<T> entityType;
    private final IEntityDao<T> companion;
    private final IEntityProducer<T> producer;
    private final EntityFactory factory;
    private final ICentreConfigSharingModel sharingModel;

    public enum EntityIdKind {
        NEW("new"), ID("id"), FIND_OR_NEW("find_or_new");

        private final String value;

        private EntityIdKind(final String value) {
            this.value = value;
        }

        boolean matches(final String value) {
            return this.value.equalsIgnoreCase(value);
        }
    }

    public EntityResource(
            final Class<T> entityType,
            final IEntityProducer<T> entityProducer,
            final EntityFactory entityFactory,
            final RestServerUtil restUtil,
            final ICriteriaGenerator critGenerator,
            final ICompanionObjectFinder companionFinder,

            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICentreConfigSharingModel sharingModel,

            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
        this.restUtil = restUtil;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.entityType = entityType;
        this.companion = companionFinder.<IEntityDao<T>, T> find(this.entityType);
        this.producer = entityProducer;
        this.factory = entityFactory;
        this.sharingModel = sharingModel;

        final String entityIdString = request.getAttributes().get("entity-id").toString();

        if (NEW.matches(entityIdString)) {
            this.entityIdKind = NEW;
            this.entityId = null;
        } else if (FIND_OR_NEW.matches(entityIdString)) {
            this.entityIdKind = FIND_OR_NEW;
            this.entityId = null;
        } else {
            this.entityIdKind = ID;
            this.entityId = Long.parseLong(entityIdString);
        }
    }

    /// Handles POST requests from `tg-entity-master` to `save()` a persisted entity.
    ///
    @Post
    public Representation save(final Representation envelope) {
        LOGGER.debug("ENTITY_RESOURCE: save started.");
        final Representation representation = handleUndesiredExceptions(getResponse(), () -> {
            final SavingInfoHolder savingInfoHolder = restoreSavingInfoHolder(envelope, restUtil);
            final User user = userProvider.getUser();
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            final Pair<T, Optional<Exception>> potentiallySavedWithException = tryToSave(savingInfoHolder, entityType, factory, companionFinder, critGenerator, webUiConfig, user, companion, device(), eccCompanion, mmiCompanion, userCompanion, sharingModel);
            return createRepresentation(potentiallySavedWithException.getKey(), potentiallySavedWithException.getValue());
        }, restUtil);
        LOGGER.debug("ENTITY_RESOURCE: save finished.");
        return representation;
    }

    /// Handles PUT requests resulting from `tg-entity-master` to `retrieve(context)` new or persisted entity.
    @Put
    public Representation retrieve(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            LOGGER.debug("ENTITY_RESOURCE: retrieve started.");
            final User user = userProvider.getUser();
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            // originallyProducedEntity is always empty during retrieval to kick in creation through producer
            final T emptyOriginallyProducedEntity = null;
            if (envelope != null) {
                if (FIND_OR_NEW == entityIdKind) {
                    final SavingInfoHolder savingInfoHolder = restoreSavingInfoHolder(envelope, restUtil);

                    final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> funcEntityType;
                    try {
                        funcEntityType = (Class<? extends AbstractFunctionalEntityWithCentreContext<?>>) Class.forName((String) savingInfoHolder.getCentreContextHolder().getCustomObject().get("@@funcEntityType"));
                    } catch (final ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                    final AbstractEntity<?> funcEntity = restoreEntityFrom(true, savingInfoHolder, funcEntityType, PropertyApplicationErrorHandler.standard, factory, webUiConfig, companionFinder, user, critGenerator, 0, device(), eccCompanion, mmiCompanion, userCompanion, sharingModel);

                    final T entity = EntityRestorationUtils.createValidationPrototypeWithContext(
                            null,
                            emptyOriginallyProducedEntity,
                            CentreResourceUtils.createCentreContext(
                                    funcEntity, /* only master context, the rest should be empty */
                                    new ArrayList<AbstractEntity<?>>(),
                                    null,
                                    Optional.empty(),
                                    null,
                                    new HashMap<>(),
                                    null
                            ),
                            companion,
                            producer
                            );
                    LOGGER.debug("ENTITY_RESOURCE: retrieve finished.");
                    return createRepresentation(entity);
                } else {
                    final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);

                    if (producer instanceof EntityShareActionProducer shareEntityActionProducer) {
                        shareEntityActionProducer.setCentreContextHolder(centreContextHolder);
                    }

                    final AbstractEntity<?> masterEntity = restoreMasterFunctionalEntity(true, webUiConfig, companionFinder, user, critGenerator, factory, centreContextHolder, 0, device(), eccCompanion, mmiCompanion, userCompanion, sharingModel);
                    final Optional<EntityActionConfig> actionConfig = restoreActionConfig(webUiConfig, centreContextHolder);

                    final T entity = EntityRestorationUtils.createValidationPrototypeWithContext(
                            null,
                            emptyOriginallyProducedEntity,
                            createCentreContext(
                                    masterEntity, /* master context */
                                    !centreContextHolder.proxiedPropertyNames().contains("selectedEntities") ? centreContextHolder.getSelectedEntities() : new ArrayList<>(),
                                    createCriteriaEntityForContext(centreContextHolder, companionFinder, user, critGenerator, webUiConfig, factory, device(), eccCompanion, mmiCompanion, userCompanion, sharingModel),
                                    actionConfig,
                                    !centreContextHolder.proxiedPropertyNames().contains("chosenProperty") ? centreContextHolder.getChosenProperty() : null,
                                    !centreContextHolder.proxiedPropertyNames().contains("customObject") ? centreContextHolder.getCustomObject() : new HashMap<>(),
                                    !centreContextHolder.proxiedPropertyNames().contains(INSTANCEBASEDCONTINUATION_PROPERTY_NAME) ? centreContextHolder.getInstanceBasedContinuation() : null
                            ),
                            companion,
                            producer
                            );
                    LOGGER.debug("ENTITY_RESOURCE: retrieve finished.");
                    return createRepresentation(entity);
                }
            } else {
                LOGGER.debug("ENTITY_RESOURCE: retrieve finished.");
                return createRepresentation(createValidationPrototype(entityId, emptyOriginallyProducedEntity, companion, producer));
            }
        }, restUtil);
    }

    /// Creates [Representation] for an entity.
    ///
    private Representation createRepresentation(final T entity) {
        return createRepresentation(entity, empty());
    }

    /// Creates [Representation] for an entity and [Optional] `exceptionOpt`.
    ///
    private Representation createRepresentation(final T entity, final Optional<Exception> exceptionOpt) {
        final Result result = restUtil.singleEntityResult(entity, exceptionOpt);
        final Map<String, Object> customObject = linkedMapOf(createPropertyActionIndicesForMaster(entity, webUiConfig));
        return restUtil.resultJSONRepresentation(result.extendResultWithCustomObject(customObject));
    }

    @Delete
    @Override
    public Representation delete() {
        return handleUndesiredExceptions(getResponse(), () -> {
            if (entityId == null) {
                final String message = "New entity was not persisted and thus can not be deleted. Actually this error should be prevented at the client-side.";
                LOGGER.error(message);
                throw new IllegalStateException(message);
            }

            return delete(entityId);
        }, restUtil);
    }

    /// Restores the entity from [SavingInfoHolder] and tries to save it.
    ///
    public static <T extends AbstractEntity<?>> Pair<T, Optional<Exception>> tryToSave(
            final SavingInfoHolder savingInfoHolder,
            final Class<T> entityType,
            final EntityFactory entityFactory,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final IWebUiConfig webUiConfig,
            final User user,
            final IEntityDao<T> companion,
            final DeviceProfile device,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        final List<IContinuationData> conts = !savingInfoHolder.proxiedPropertyNames().contains("continuations") ? savingInfoHolder.getContinuations() : new ArrayList<>();
        final List<String> contProps = !savingInfoHolder.proxiedPropertyNames().contains("continuationProperties") ? savingInfoHolder.getContinuationProperties() : new ArrayList<>();
        final Map<String, IContinuationData> continuations = conts != null && !conts.isEmpty() ?
                EntityResourceContinuationsHelper.createContinuationsMap(conts, contProps) : new LinkedHashMap<>();
        final T applied = restoreEntityFrom(false, savingInfoHolder, entityType, PropertyApplicationErrorHandler.standard, entityFactory, webUiConfig, companionFinder, user, critGenerator, 0, device, eccCompanion, mmiCompanion, userCompanion, sharingModel);

        return EntityResourceContinuationsHelper.saveWithContinuations(applied, continuations, companion);
    }

    /// Restores the functional entity from the `savingInfoHolder`, that represents it.
    /// The `savingInfoHolder` could potentially contain `contreContextHolder` inside, which will also be deserialised.
    ///
    /// All parameters, except `savingInfoHolder` and `functionalEntityType`, could be taken from the injector -- they are needed for deserialising the centre context.
    ///
    /// @param disregardOriginallyProducedEntities indicates whether it is necessary to disregard originallyProducedEntity while restoring this entity and its parent functional entities
    /// @param savingInfoHolder                    the actual holder of information about functional entity
    /// @param functionalEntityType                the type of functional entity to be restored into
    /// @param propApplicationErrorHandler         an error handler that applies only to the restoration of the resulting entity
    ///
    public static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final boolean disregardOriginallyProducedEntities,
            final SavingInfoHolder savingInfoHolder,
            final Class<T> functionalEntityType,
            final PropertyApplicationErrorHandler propApplicationErrorHandler,
            final EntityFactory entityFactory,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final User user,
            final ICriteriaGenerator critGenerator,
            final int tabCount,
            final DeviceProfile device,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        final DateTime start = new DateTime();
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): started.");
        final EntityMaster<T> master = (EntityMaster<T>) webUiConfig.getMasters().get(functionalEntityType);
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): master.");
        final IEntityProducer<T> entityProducer = master.createEntityProducer();
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): producer.");
        final IEntityDao<T> companion = companionFinder.<IEntityDao<T>, T> find(functionalEntityType);
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): utils.");
        final CentreContextHolder centreContextHolder = !savingInfoHolder.proxiedPropertyNames().contains("centreContextHolder") ? savingInfoHolder.getCentreContextHolder() : null;
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): master entity restore...");
        final AbstractEntity<?> funcEntity = restoreMasterFunctionalEntity(disregardOriginallyProducedEntities, webUiConfig, companionFinder, user, critGenerator, entityFactory, centreContextHolder, tabCount + 1, device, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): master entity has been restored.");
        final T restored = restoreEntityFrom(disregardOriginallyProducedEntities, webUiConfig, user, savingInfoHolder, propApplicationErrorHandler, entityFactory, functionalEntityType, companion, entityProducer, companionFinder, critGenerator, funcEntity /* master context */, tabCount + 1, device, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): duration: " + pd.getSeconds() + " s " + pd.getMillis() + " ms.");
        return restored;
    }

    public static AbstractEntity<?> restoreMasterFunctionalEntity(
            final boolean disregardOriginallyProducedEntities,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final User user,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final int tabCount,
            final DeviceProfile device,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        LOGGER.debug(() -> tabs(tabCount) + "restoreMasterFunctionalEntity: started.");
        final DateTime start = new DateTime();
        AbstractEntity<?> entity = null;
        if (centreContextHolder != null && !centreContextHolder.proxiedPropertyNames().contains("masterEntity") && centreContextHolder.getMasterEntity() instanceof SavingInfoHolder) {
            final SavingInfoHolder outerContext = (SavingInfoHolder) centreContextHolder.getMasterEntity();
            final Class<? extends AbstractEntity<?>> entityType;
            try {
                final CentreContextHolder cch = !outerContext.proxiedPropertyNames().contains("centreContextHolder") ? outerContext.getCentreContextHolder() : null;
                if (cch != null && cch.getCustomObject().get("@@funcEntityType") != null) {
                    entityType = (Class<? extends AbstractEntity<?>>) Class.forName((String) cch.getCustomObject().get("@@funcEntityType"));
                } else {
                    entityType = null;
                }
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }

            if (entityType != null) {
                entity = restoreEntityFrom(disregardOriginallyProducedEntities, outerContext, entityType, PropertyApplicationErrorHandler.standard, entityFactory, webUiConfig, companionFinder, user, critGenerator, tabCount + 1, device, eccCompanion, mmiCompanion, userCompanion, sharingModel);
            }
        }
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);

        LOGGER.debug(() -> tabs(tabCount) + "restoreMasterFunctionalEntity: duration: " + pd.getSeconds() + " s " + pd.getMillis() + " ms.");
        return entity;
    }

    private static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final boolean disregardOriginallyProducedEntities,
            final IWebUiConfig webUiConfig,
            final User user,
            final SavingInfoHolder savingInfoHolder,
            final PropertyApplicationErrorHandler propApplicationErrorHandler,
            final EntityFactory entityFactory,
            final Class<T> entityType,
            final IEntityDao<T> companion,
            final IEntityProducer<T> producer,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final AbstractEntity<?> masterContext,
            final int tabCount,
            final DeviceProfile device,
            final EntityCentreConfigCo eccCompanion,
            final MainMenuItemCo mmiCompanion,
            final IUser userCompanion,
            final ICentreConfigSharingModel sharingModel) {
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): started.");
        final Map<String, Object> modifiedPropertiesHolder = savingInfoHolder.getModifHolder();
        final T originallyProducedEntity = disregardOriginallyProducedEntities ? null : // in case where full context should be used for entity restoration -- originallyProducedEntity will be disregarded
            (!savingInfoHolder.proxiedPropertyNames().contains("originallyProducedEntity") ? (T) savingInfoHolder.getOriginallyProducedEntity() : null);
        final T applied;
        final CentreContextHolder centreContextHolder = !savingInfoHolder.proxiedPropertyNames().contains("centreContextHolder") ? savingInfoHolder.getCentreContextHolder() : null;
        if (centreContextHolder == null) {
            //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder.");
            applied = EntityRestorationUtils.constructEntity(modifiedPropertiesHolder, propApplicationErrorHandler, originallyProducedEntity, companion, producer, companionFinder).getKey();
            //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder finished.");
        } else {
            //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder started.");
            final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> criteriaEntity = createCriteriaEntityForContext(centreContextHolder, companionFinder, user, critGenerator, webUiConfig, entityFactory, device, eccCompanion, mmiCompanion, userCompanion, sharingModel);

            //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder started. criteriaEntity.");
            final Optional<EntityActionConfig> actionConfig = restoreActionConfig(webUiConfig, centreContextHolder);

            final CentreContext<T, AbstractEntity<?>> centreContext = CentreResourceUtils.createCentreContext(
                    masterContext,
                    !centreContextHolder.proxiedPropertyNames().contains("selectedEntities") ? centreContextHolder.getSelectedEntities() : new ArrayList<>(),
                    criteriaEntity,
                    actionConfig,
                    !centreContextHolder.proxiedPropertyNames().contains("chosenProperty") ? centreContextHolder.getChosenProperty() : null,
                    !centreContextHolder.proxiedPropertyNames().contains("customObject") ? centreContextHolder.getCustomObject() : new HashMap<>(),
                    !centreContextHolder.proxiedPropertyNames().contains(INSTANCEBASEDCONTINUATION_PROPERTY_NAME) ? centreContextHolder.getInstanceBasedContinuation() : null
                    );
            //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder started. centreContext.");

            applied = EntityRestorationUtils.constructEntityWithContext(
                    modifiedPropertiesHolder,
                    propApplicationErrorHandler,
                    originallyProducedEntity,
                    centreContext,
                    tabCount + 1,
                    companion,
                    producer,
                    companionFinder
                    ).getKey();
            //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder finished.");
        }
        //LOGGER.debug(tabs(tabCount) + "restoreEntityFrom (PRIVATE): finished.");
        return applied;
    }

    /// In case where `centreContextHolder` represents a context of centre's action (top-level, primary, secondary or prop) -- this method determines the action configuration.
    /// Action configuration is necessary to be used for the "computation" part of the context.
    ///
    public static <T extends AbstractEntity<?>> Optional<EntityActionConfig> restoreActionConfig(final IWebUiConfig webUiConfig, final CentreContextHolder centreContextHolder) {
        final Optional<EntityActionConfig> actionConfig;
        if (centreContextHolder.getCustomObject().get("@@miType") != null && centreContextHolder.getCustomObject().get("@@actionNumber") != null && centreContextHolder.getCustomObject().get("@@actionKind") != null) {
            final Class<? extends MiWithConfigurationSupport<?>> miType;
            try {
                miType = (Class<? extends MiWithConfigurationSupport<?>>) Class.forName((String) centreContextHolder.getCustomObject().get("@@miType"));
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            final EntityCentre<T> centre = (EntityCentre<T>) webUiConfig.getCentres().get(miType);
            actionConfig = ofNullable(centre.actionConfig(
                valueOf((String) centreContextHolder.getCustomObject().get("@@actionKind")),
                Integer.valueOf((Integer) centreContextHolder.getCustomObject().get("@@actionNumber")
            )));
        } else if (centreContextHolder.getCustomObject().get("@@masterEntityType") != null && centreContextHolder.getCustomObject().get("@@actionNumber") != null && centreContextHolder.getCustomObject().get("@@actionKind") != null) {
            final Class<?> entityType;
            try {
                entityType = Class.forName((String) centreContextHolder.getCustomObject().get("@@masterEntityType"));
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            final EntityMaster<T> master = (EntityMaster<T>) webUiConfig.getMasters().get(entityType);
            actionConfig = ofNullable(master.actionConfig(
                valueOf((String) centreContextHolder.getCustomObject().get("@@actionKind")),
                Integer.valueOf((Integer) centreContextHolder.getCustomObject().get("@@actionNumber")))
            );
        } else {
            actionConfig = empty();
        }
        return actionConfig;
    }

    /// Tries to delete the entity with `entityId`.
    ///
    /// @return a representation of a successful [Result].
    ///
    /// @throws InvalidStateException indicates an error during deletion.
    ///
    private Representation delete(final Long entityId) {
        try {
            companion.delete(factory.newEntity(entityType, entityId));
            return restUtil.resultJSONRepresentation(successful());
        } catch (final Exception ex) {
            throw new InvalidStateException(ERR_ENTITY_DELETION.formatted(entityId, entityType.getSimpleName()), ex);
        }
    }

}
