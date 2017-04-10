package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.FIND_OR_NEW;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.ID;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.NEW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityResourceContinuationsHelper;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;
import ua.com.fielden.platform.web.utils.EntityRestorationUtils;
import ua.com.fielden.platform.web.utils.WebUiResourceUtils;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The web resource for entity serves as a back-end mechanism of entity retrieval, saving and deletion. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieve entity -- GET request;
 * <li>save new entity -- PUT request with an envelope containing an instance of an entity to be persisted;
 * <li>save already persisted entity -- POST request with an envelope containing an instance of an modified entity to be changed;
 * <li>delete entity -- DELETE request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class EntityResource<T extends AbstractEntity<?>> extends ServerResource {
    private final RestServerUtil restUtil;
    private final Long entityId;
    private final EntityIdKind entityIdKind;
    private final static Logger logger = Logger.getLogger(EntityResource.class);

    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final Class<T> entityType;
    private final IEntityDao<T> companion;
    private final IEntityProducer<T> producer;
    private final EntityFactory factory;

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
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
        this.restUtil = restUtil;
        this.webUiConfig = webUiConfig;
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;
        this.entityType = entityType;
        this.companion = companionFinder.<IEntityDao<T>, T> find(this.entityType);
        this.producer = entityProducer;
        this.factory = entityFactory;

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

    /**
     * Handles POST requests resulting from tg-entity-master <code>save()</code> method (persisted entity).
     */
    @Post
    public Representation save(final Representation envelope) {
        logger.debug("ENTITY_RESOURCE: save started.");
        final Representation result = WebUiResourceUtils.handleUndesiredExceptions(getResponse(), () -> tryToSave(envelope), restUtil);
        logger.debug("ENTITY_RESOURCE: save finished.");
        return result;
    }

    /**
     * Handles PUT requests resulting from tg-entity-master <code>retrieve(context)</code> method (new or persisted entity).
     */
    @Put
    public Representation retrieve(final Representation envelope) {
        return WebUiResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("ENTITY_RESOURCE: retrieve started.");
            // originallyProducedEntity is always empty during retrieval to kick in creation through producer
            final T emptyOriginallyProducedEntity = null;
            if (envelope != null) {
                if (FIND_OR_NEW == entityIdKind) {
                    final SavingInfoHolder savingInfoHolder = WebUiResourceUtils.restoreSavingInfoHolder(envelope, restUtil);

                    final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> funcEntityType;
                    try {
                        funcEntityType = (Class<? extends AbstractFunctionalEntityWithCentreContext<?>>) Class.forName((String) savingInfoHolder.getCentreContextHolder().getCustomObject().get("@@funcEntityType"));
                    } catch (final ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                    final AbstractEntity<?> funcEntity = EntityResource.restoreEntityFrom(savingInfoHolder, funcEntityType, factory, webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, 0);

                    final T entity = EntityRestorationUtils.createValidationPrototypeWithContext(null, emptyOriginallyProducedEntity, null, null, null, funcEntity, companion, producer);
                    logger.debug("ENTITY_RESOURCE: retrieve finished.");
                    return restUtil.rawListJSONRepresentation(entity);
                } else {
                    final CentreContextHolder centreContextHolder = WebUiResourceUtils.restoreCentreContextHolder(envelope, restUtil);

                    final AbstractEntity<?> masterEntity = restoreMasterFunctionalEntity(webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, factory, centreContextHolder, 0);
                    final Optional<EntityActionConfig> actionConfig = EntityResource.restoreActionConfig(webUiConfig, centreContextHolder);

                    final T entity = EntityRestorationUtils.createValidationPrototypeWithContext(
                            null,
                            emptyOriginallyProducedEntity,
                            CentreResourceUtils.createCentreContext(
                                    companionFinder,
                                    serverGdtm,
                                    userProvider,
                                    critGenerator,
                                    factory,
                                    masterEntity,
                                    !centreContextHolder.proxiedPropertyNames().contains("selectedEntities") ? centreContextHolder.getSelectedEntities() : new ArrayList<AbstractEntity<?>>(),
                                    CentreResourceUtils.createCriteriaEntityForContext(centreContextHolder, companionFinder, ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider), critGenerator),
                                    actionConfig
                            ),
                            !centreContextHolder.proxiedPropertyNames().contains("chosenProperty") ? centreContextHolder.getChosenProperty() : null,
                            null /* compound master entity id */,
                            masterEntity, /* master context */
                            companion, 
                            producer
                            );
                    logger.debug("ENTITY_RESOURCE: retrieve finished.");
                    return restUtil.rawListJSONRepresentation(EntityResourceUtils.resetContextBeforeSendingToClient(entity));
                }
            } else {
                logger.debug("ENTITY_RESOURCE: retrieve finished.");
                return restUtil.rawListJSONRepresentation(EntityRestorationUtils.createValidationPrototype(entityId, emptyOriginallyProducedEntity, companion, producer));
            }
        }, restUtil);
    }

    @Delete
    @Override
    public Representation delete() {
        return WebUiResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            if (entityId == null) {
                final String message = String.format("New entity was not persisted and thus can not be deleted. Actually this error should be prevented at the client-side.");
                logger.error(message);
                throw new IllegalStateException(message);
            }

            return delete(entityId);
        }, restUtil);
    }

    /**
     * Tries to save the changes for the entity and returns it in JSON format.
     *
     * @param envelope
     * @return
     */
    private Representation tryToSave(final Representation envelope) {
        final SavingInfoHolder savingInfoHolder = WebUiResourceUtils.restoreSavingInfoHolder(envelope, restUtil);
        final Pair<T, Optional<Exception>> potentiallySavedWithException = tryToSave(savingInfoHolder);
        return restUtil.singleJSONRepresentation(EntityResourceUtils.resetContextBeforeSendingToClient(potentiallySavedWithException.getKey()), potentiallySavedWithException.getValue());
    }

    private Pair<T, Optional<Exception>> tryToSave(final SavingInfoHolder savingInfoHolder) {
        return tryToSave(savingInfoHolder, entityType, factory, companionFinder, critGenerator, webUiConfig, serverGdtm, userProvider, companion);
    }
    
    /**
     * Restores the entity from {@link SavingInfoHolder} and tries to save it.
     * 
     * @param savingInfoHolder
     * @param entityType
     * @param entityFactory
     * @param companionFinder
     * @param critGenerator
     * @param webUiConfig
     * @param serverGdtm
     * @param userProvider
     * @param companion
     * @return
     */
    public static <T extends AbstractEntity<?>> Pair<T, Optional<Exception>> tryToSave(
        final SavingInfoHolder savingInfoHolder,
        final Class<T> entityType,
        final EntityFactory entityFactory,
        final ICompanionObjectFinder companionFinder,
        final ICriteriaGenerator critGenerator,
        final IWebUiConfig webUiConfig,
        final IServerGlobalDomainTreeManager serverGdtm,
        final IUserProvider userProvider,
        final IEntityDao<T> companion
    ) {
        final List<IContinuationData> conts = !savingInfoHolder.proxiedPropertyNames().contains("continuations") ? savingInfoHolder.getContinuations() : new ArrayList<>();
        final List<String> contProps = !savingInfoHolder.proxiedPropertyNames().contains("continuationProperties") ? savingInfoHolder.getContinuationProperties() : new ArrayList<>();
        final Map<String, IContinuationData> continuations = conts != null && !conts.isEmpty() ?
                EntityResourceContinuationsHelper.createContinuationsMap(conts, contProps) : new LinkedHashMap<>();
        final T applied = EntityResource.restoreEntityFrom(savingInfoHolder, entityType, entityFactory, webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, 0);

        final Pair<T, Optional<Exception>> potentiallySavedWithException = EntityResourceContinuationsHelper.saveWithContinuations(applied, continuations, companion);
        return potentiallySavedWithException;
    }

    /**
     * Restores the functional entity from the <code>savingInfoHolder</code>, that represents it. The <code>savingInfoHolder</code> could potentially contain
     * <code>contreContextHolder</code> inside, which will be deserialised as well.
     * <p>
     * All parameters, except <code>savingInfoHolder</code> and <code>functionalEntityType</code>, could be taken from injector -- they are needed for centre context
     * deserialisation.
     *
     * @param savingInfoHolder
     *            -- the actual holder of information about functional entity
     * @param functionalEntityType
     *            -- the type of functional entity to be restored into
     * @param entityFactory
     * @param webUiConfig
     * @param companionFinder
     * @param serverGdtm
     * @param userProvider
     * @param critGenerator
     * @param tabCount
     * @return
     */
    public static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final SavingInfoHolder savingInfoHolder,
            final Class<T> functionalEntityType,
            final EntityFactory entityFactory,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator, final int tabCount) {
        final DateTime start = new DateTime();
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): started.");
        final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): gdtm.");
        final EntityMaster<T> master = (EntityMaster<T>) webUiConfig.getMasters().get(functionalEntityType);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): master.");
        final IEntityProducer<T> entityProducer = master.createEntityProducer();
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): producer.");
        final IEntityDao<T> companion = companionFinder.<IEntityDao<T>, T> find(functionalEntityType);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): utils.");
        final CentreContextHolder centreContextHolder = !savingInfoHolder.proxiedPropertyNames().contains("centreContextHolder") ? savingInfoHolder.getCentreContextHolder() : null;
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): master entity restore...");
        final AbstractEntity<?> funcEntity = restoreMasterFunctionalEntity(webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, entityFactory, centreContextHolder, tabCount + 1);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): master entity has been restored.");
        final T restored = restoreEntityFrom(webUiConfig, serverGdtm, userProvider, savingInfoHolder, entityFactory, functionalEntityType, companion, entityProducer, companionFinder, gdtm, critGenerator, funcEntity /* master context */, tabCount + 1);
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (" + functionalEntityType.getSimpleName() + "): duration: " + pd.getSeconds() + " s " + pd.getMillis() + " ms.");
        return restored;
    }

    public static AbstractEntity<?> restoreMasterFunctionalEntity(
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder, final int tabCount) {
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreMasterFunctionalEntity: started.");
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
                entity = EntityResource.restoreEntityFrom(outerContext, entityType, entityFactory, webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, tabCount + 1);
            }
        }
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);

        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreMasterFunctionalEntity: duration: " + pd.getSeconds() + " s " + pd.getMillis() + " ms.");
        return entity;
    }

    private static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final IWebUiConfig webUiConfig,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final SavingInfoHolder savingInfoHolder,
            final EntityFactory entityFactory,
            final Class<T> entityType,
            final IEntityDao<T> companion,
            final IEntityProducer<T> producer,
            final ICompanionObjectFinder companionFinder,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,
            final AbstractEntity<?> masterContext,
            final int tabCount) {
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): started.");
        final Map<String, Object> modifiedPropertiesHolder = savingInfoHolder.getModifHolder();
        final T originallyProducedEntity = !savingInfoHolder.proxiedPropertyNames().contains("originallyProducedEntity") ? (T) savingInfoHolder.getOriginallyProducedEntity() : null;
        final T applied;
        final CentreContextHolder centreContextHolder = !savingInfoHolder.proxiedPropertyNames().contains("centreContextHolder") ? savingInfoHolder.getCentreContextHolder() : null;
        if (centreContextHolder == null) {
            logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder.");
            applied = EntityRestorationUtils.constructEntity(modifiedPropertiesHolder, originallyProducedEntity, companion, producer, companionFinder).getKey();
            logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder finished.");
        } else {
            final Object compoundMasterEntityIdRaw = centreContextHolder.getCustomObject().get("@@compoundMasterEntityId");
            final Long compoundMasterEntityId = compoundMasterEntityIdRaw == null ? null : Long.parseLong(compoundMasterEntityIdRaw.toString());

            logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder started.");
            final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> criteriaEntity = CentreResourceUtils.createCriteriaEntityForContext(centreContextHolder, companionFinder, gdtm, critGenerator);

            if (criteriaEntity != null) {
                criteriaEntity.setExportQueryRunner((final Map<String, Object> customObject) -> {
                    final Class<? extends MiWithConfigurationSupport<?>> miType = EntityResourceUtils.getMiType((Class<EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>>) criteriaEntity.getClass());
                    final EntityCentre<AbstractEntity<?>> centre = (EntityCentre<AbstractEntity<?>>) webUiConfig.getCentres().get(miType);
                    customObject.putAll(centreContextHolder.getCustomObject());
                    // at this stage (during exporting of centre data) appliedCriteriaEntity is valid, because it represents 'previouslyRun' centre criteria which is getting updated only if Run was initiated and selection criteria validation succeeded
                    final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>>) criteriaEntity;
                    // if the export() invocation occurs on the centre that warrants data generation
                    // then for an entity centre configuration check if a generator was provided
                    final boolean createdByConstraintShouldOccur = centre.getGeneratorTypes().isPresent();

                    final Pair<Map<String, Object>, List<?>> pair =
                            CentreResourceUtils.createCriteriaMetaValuesCustomObjectWithResult(
                                    customObject,
                                    appliedCriteriaEntity,
                                    centre.getAdditionalFetchProvider(),
                                    CriteriaResource.createQueryEnhancerAndContext(
                                            webUiConfig,
                                            companionFinder,
                                            serverGdtm,
                                            userProvider,
                                            critGenerator,
                                            entityFactory,
                                            centreContextHolder,
                                            centre.getQueryEnhancerConfig(),
                                            appliedCriteriaEntity),
                                    // There could be cases where the generated data and the queried data would have different types.
                                    // For example, the queried data could be modelled by a synthesized entity that includes a subquery based on some generated data.
                                    // In such cases, it is unpossible to enhance the final query with a user related condition automatically.
                                    // This should be the responsibility of the application developer to properly construct a subquery that is based on the generated data.
                                    // The query will be enhanced with condition createdBy=currentUser if createdByConstraintShouldOccur and generatorEntityType equal to the type of queried data (otherwise end-developer should do that itself by using queryEnhancer or synthesized model).
                                    createdByConstraintShouldOccur && centre.getGeneratorTypes().get().getKey().equals(EntityResourceUtils.getEntityType(miType)) ? Optional.of(userProvider.getUser()) : Optional.empty());

                    if (pair.getValue() == null) {
                        return new ArrayList<AbstractEntity<?>>();
                    } else {
                        CriteriaResource.enhanceResultEntitiesWithCustomPropertyValues(centre, centre.getCustomPropertiesDefinitions(), centre.getCustomPropertiesAsignmentHandler(), (List<AbstractEntity<?>>) pair.getValue());
                        return (List<AbstractEntity<?>>) pair.getValue();
                    }
                });
            }

            logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder started. criteriaEntity.");
            final Optional<EntityActionConfig> actionConfig = restoreActionConfig(webUiConfig, centreContextHolder);

            final CentreContext<T, AbstractEntity<?>> centreContext = CentreResourceUtils.createCentreContext(
                    companionFinder,
                    serverGdtm,
                    userProvider,
                    critGenerator,
                    entityFactory,
                    masterContext,
                    !centreContextHolder.proxiedPropertyNames().contains("selectedEntities") ? centreContextHolder.getSelectedEntities() : new ArrayList<AbstractEntity<?>>(),
                    criteriaEntity,
                    actionConfig);
            logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder started. centreContext.");
            
            applied = EntityRestorationUtils.constructEntityWithContext(
                    modifiedPropertiesHolder,
                    originallyProducedEntity,
                    centreContext,
                    !centreContextHolder.proxiedPropertyNames().contains("chosenProperty") ? centreContextHolder.getChosenProperty() : null,
                    compoundMasterEntityId,
                    masterContext, tabCount + 1,
                    companion,
                    producer,
                    companionFinder
                    ).getKey();
            logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): constructEntity from modifiedPropertiesHolder+centreContextHolder finished.");
        }
        logger.debug(EntityResourceUtils.tabs(tabCount) + "restoreEntityFrom (PRIVATE): finished.");
        return applied;
    }

    /**
     * In case where centreContextHolder represents the context of centre's action (top-level, primary, secondary or prop) -- this method determines the action configuration.
     * Action configuration is necessary to be used for 'computation' part of the context.
     *
     * @param webUiConfig
     * @param centreContextHolder
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<EntityActionConfig> restoreActionConfig(final IWebUiConfig webUiConfig, final CentreContextHolder centreContextHolder) {
        final Optional<EntityActionConfig> actionConfig;
        if (centreContextHolder.getCustomObject().get("@@miType") != null && centreContextHolder.getCustomObject().get("@@actionNumber") != null && centreContextHolder.getCustomObject().get("@@actionKind") != null) {
            // System.err.println("===========miType = " + centreContextHolder.getCustomObject().get("@@miType") + "=======ACTION_IDENTIFIER = [" + centreContextHolder.getCustomObject().get("@@actionKind") + "; " + centreContextHolder.getCustomObject().get("@@actionNumber") + "]");

            final Class<? extends MiWithConfigurationSupport<?>> miType;
            try {
                miType = (Class<? extends MiWithConfigurationSupport<?>>) Class.forName((String) centreContextHolder.getCustomObject().get("@@miType"));
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            final EntityCentre<T> centre = (EntityCentre<T>) webUiConfig.getCentres().get(miType);
            actionConfig = Optional.of(centre.actionConfig(
                                FunctionalActionKind.valueOf((String) centreContextHolder.getCustomObject().get("@@actionKind")),
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
            actionConfig = Optional.of(master.actionConfig(
                                FunctionalActionKind.valueOf((String) centreContextHolder.getCustomObject().get("@@actionKind")),
                                Integer.valueOf((Integer) centreContextHolder.getCustomObject().get("@@actionNumber")
                            )));
        } else {
            actionConfig = Optional.empty();
        }
        return actionConfig;
    }
    
    /**
     * Tries to delete the entity with <code>entityId</code> and returns result. If successful -- result instance is <code>null</code>, otherwise -- result instance is also
     * <code>null</code> (not-deletable entity should exist at the client side, no need to send it many times).
     *
     * @param entityId
     *
     * @return
     * @throws JsonProcessingException
     */
    private Representation delete(final Long entityId) {
        try {
            companion.delete(factory.newEntity(entityType, entityId));
            return restUtil.resultJSONRepresentation(Result.successful(null));
        } catch (final Exception e) {
            final String message = String.format("The entity with id [%s] and type [%s] can not be deleted due to existing dependencies.", entityId, entityType.getSimpleName());
            logger.error(message, e);
            throw new IllegalStateException(e);
        }
    }

    public static Logger logger() {
        return logger;
    }
}
