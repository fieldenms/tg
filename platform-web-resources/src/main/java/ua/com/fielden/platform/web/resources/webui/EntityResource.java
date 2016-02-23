package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.FIND_OR_NEW;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.ID;
import static ua.com.fielden.platform.web.resources.webui.EntityResource.EntityIdKind.NEW;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.view.master.EntityMaster;

import com.fasterxml.jackson.core.JsonProcessingException;

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
    private final EntityResourceUtils<T> utils;
    private final RestServerUtil restUtil;
    private final Long entityId;
    private final EntityIdKind entityIdKind;
    private final Logger logger = Logger.getLogger(getClass());

    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;


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
        utils = new EntityResourceUtils<T>(entityType, entityProducer, entityFactory, this.companionFinder);
        this.restUtil = restUtil;
        this.webUiConfig = webUiConfig;
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;

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
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> tryToSave(envelope), restUtil);
    }

    /**
     * Handles PUT requests resulting from tg-entity-master <code>retrieve(context)</code> method (new or persisted entity).
     */
    @Put
    public Representation retrieve(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            if (envelope != null) {
                if (FIND_OR_NEW == entityIdKind) {
                    final SavingInfoHolder savingInfoHolder = EntityResourceUtils.restoreSavingInfoHolder(envelope, restUtil);

                    final Class<? extends AbstractFunctionalEntityWithCentreContext<?>> funcEntityType;
                    try {
                        funcEntityType = (Class<? extends AbstractFunctionalEntityWithCentreContext<?>>) Class.forName((String) savingInfoHolder.getCentreContextHolder().getCustomObject().get("@@funcEntityType"));
                    } catch (final ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                    final AbstractEntity<?> funcEntity = EntityResource.restoreEntityFrom(savingInfoHolder, funcEntityType, utils.entityFactory(), webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator);

                    final Long intendedId;
                    if (EntityEditAction.class.isAssignableFrom(funcEntity.getClass())) {
                        intendedId = Long.valueOf(((EntityEditAction) funcEntity).getEntityId());
                    } else {
                        intendedId = null;
                    }
                    final T entity = utils.createValidationPrototypeWithContext(intendedId, null, null, null, funcEntity);
                    return restUtil.rawListJSONRepresentation(entity);
                } else {
                    final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

                    final AbstractEntity<?> masterEntity = restoreMasterFunctionalEntity(webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, utils.entityFactory(), centreContextHolder);


                    final T entity = utils.createValidationPrototypeWithContext(
                            null,
                            CentreResourceUtils.createCentreContext(
                                    webUiConfig,
                                    companionFinder,
                                    serverGdtm,
                                    userProvider,
                                    critGenerator,
                                    utils.entityFactory(),
                                    centreContextHolder,
                                    CentreResourceUtils.createCriteriaEntity(centreContextHolder, companionFinder, ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider), critGenerator)//
                            ),
                            centreContextHolder.getChosenProperty(),
                            null /* compound master entity id */,
                            masterEntity /* master context */
                    );
                    ((AbstractFunctionalEntityWithCentreContext) entity).setContext(null); // it is necessary to reset centreContext not to send it back to the client!
                    return restUtil.rawListJSONRepresentation(entity);
                }
            } else {
                return restUtil.rawListJSONRepresentation(utils.createValidationPrototype(entityId));
            }
        }, restUtil);
    }

    @Delete
    @Override
    public Representation delete() {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
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
        final SavingInfoHolder savingInfoHolder = EntityResourceUtils.restoreSavingInfoHolder(envelope, restUtil);
        final T applied = EntityResource.restoreEntityFrom(savingInfoHolder, utils.getEntityType(), utils.entityFactory(), webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator);


        final T potentiallySaved = applied.isDirty() ? save(applied) : applied;
        if (savingInfoHolder.getCentreContextHolder() != null && potentiallySaved instanceof AbstractFunctionalEntityWithCentreContext) {
            ((AbstractFunctionalEntityWithCentreContext) potentiallySaved).setContext(null); // it is necessary to reset centreContext not to send it back to the client!
        }
        return restUtil.singleJSONRepresentation(potentiallySaved);
    }

    /**
     * Restores the functional entity from the <code>savingInfoHolder</code>, that represents it. The <code>savingInfoHolder</code> could potentially contain <code>contreContextHolder</code> inside, which will be deserialised as well.
     * <p>
     * All parameters, except <code>savingInfoHolder</code> and <code>functionalEntityType</code>, could be taken from injector -- they are needed for centre context deserialisation.
     *
     * @param savingInfoHolder -- the actual holder of information about functional entity
     * @param functionalEntityType -- the type of functional entity to be restored into
     * @param entityFactory
     * @param webUiConfig
     * @param companionFinder
     * @param serverGdtm
     * @param userProvider
     * @param critGenerator
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
            final ICriteriaGenerator critGenerator) {
        final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
        final EntityMaster<T> master = (EntityMaster<T>) webUiConfig.getMasters().get(functionalEntityType);
        final EntityResourceUtils<T> utils = new EntityResourceUtils<T>(functionalEntityType, master.createEntityProducer(), entityFactory, companionFinder);
        final Map<String, Object> modifHolder = savingInfoHolder.getModifHolder();

        final Object arrivedIdVal = modifHolder.get(AbstractEntity.ID);
        final Long longId = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");


        final CentreContextHolder centreContextHolder = savingInfoHolder.getCentreContextHolder();
        final AbstractEntity<?> funcEntity = restoreMasterFunctionalEntity(webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator, utils.entityFactory(), centreContextHolder);

        return restoreEntityFrom(webUiConfig, serverGdtm, userProvider, savingInfoHolder, utils, longId, companionFinder, gdtm, critGenerator, funcEntity /* master context */);
    }

    public static AbstractEntity<?> restoreMasterFunctionalEntity(
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder) {
        AbstractEntity<?> entity = null;
        if (centreContextHolder != null && centreContextHolder.getMasterEntity() instanceof SavingInfoHolder) {
            final SavingInfoHolder outerContext = (SavingInfoHolder) centreContextHolder.getMasterEntity();
            final Class<? extends AbstractEntity<?>> entityType;
            try {
                final CentreContextHolder cch = outerContext.getCentreContextHolder();
                if (cch != null && cch.getCustomObject().get("@@funcEntityType") != null) {
                    entityType = (Class<? extends AbstractEntity<?>>) Class.forName((String) cch.getCustomObject().get("@@funcEntityType"));
                } else {
                    entityType = null;
                }
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }

            if (entityType != null) {
                entity = EntityResource.restoreEntityFrom(outerContext, entityType, entityFactory, webUiConfig, companionFinder, serverGdtm, userProvider, critGenerator);
            }
        }
        return entity;
    }

    private static <T extends AbstractEntity<?>> T restoreEntityFrom(
            final IWebUiConfig webUiConfig,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final SavingInfoHolder savingInfoHolder,
            final EntityResourceUtils<T> utils,
            final Long entityId,
            final ICompanionObjectFinder companionFinder,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,
            final AbstractEntity<?> masterContext) {
        final Map<String, Object> modifiedPropertiesHolder = savingInfoHolder.getModifHolder();
        final T applied;
        if (savingInfoHolder.getCentreContextHolder() == null) {
            applied = utils.constructEntity(modifiedPropertiesHolder, entityId).getKey();
        } else {
            final Object compoundMasterEntityIdRaw = savingInfoHolder.getCentreContextHolder().getCustomObject().get("@@compoundMasterEntityId");
            final Long compoundMasterEntityId = compoundMasterEntityIdRaw == null ? null : Long.parseLong(compoundMasterEntityIdRaw.toString());

            applied = utils.constructEntity(
                    modifiedPropertiesHolder,
                    CentreResourceUtils.createCentreContext(
                            webUiConfig,
                            companionFinder,
                            serverGdtm,
                            userProvider,
                            critGenerator,
                            utils.entityFactory(),
                            savingInfoHolder.getCentreContextHolder(),
                            CentreResourceUtils.createCriteriaEntity(savingInfoHolder.getCentreContextHolder(), companionFinder, gdtm, critGenerator)),
                    savingInfoHolder.getCentreContextHolder().getChosenProperty(),
                    compoundMasterEntityId,
                    masterContext
           ).getKey();
        }
        return applied;
    }

    /**
     * Checks the entity on validation errors and saves it if validation was successful, returns it "as is" if validation was not successful.
     *
     * @param validatedEntity
     * @return
     */
    private T save(final T validatedEntity) {
        T savedEntity;
        try {
            // try to save the entity with its companion 'save' method
            savedEntity = utils.save(validatedEntity);
        } catch (final Result result) {
            // some result can be thrown inside 1) its companion 'save' method OR 2) CommonEntityDao 'save' during its internal validation
            if (!validatedEntity.isValid().isSuccessful()) {
                // if entity is invalid after its unsuccessful save -- return invalid entity back to the client
                return validatedEntity;
            } else {
                // if entity is valid after its unsuccessful save -- just throw result further
                throw result;
            }
        }

        return savedEntity;
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
            utils.delete(entityId);
            return restUtil.resultJSONRepresentation(Result.successful(null));
        } catch (final Exception e) {
            final String message = String.format("The entity with id [%s] and type [%s] can not be deleted due to existing dependencies.", entityId, utils.getEntityType().getSimpleName());
            logger.error(message, e);
            throw new IllegalStateException(e);
        }
    }

}
