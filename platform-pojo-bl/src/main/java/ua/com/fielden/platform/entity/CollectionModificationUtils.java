package ua.com.fielden.platform.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Contains a set of utilities to implement collection modification functional entities.
 * These utilities should be used in producers and companion objects.
 * 
 * @author TG Team
 *
 */
public class CollectionModificationUtils {
    private static final String TRY_AGAIN_MSG = "Please cancel this action and try again.";
    
    private CollectionModificationUtils() {
    }
    
    /**
     * Validates action before its saving.
     * <p>
     * Validation includes: early throwing of exception if validation error exists,
     * full validation of master entity (existence, dirtiness and disappearance),
     * validation on available items deletion,
     * validation on action conflict against same master entity.
     * 
     * @param staleAction
     * @param companion
     * @param idType
     * @param controller
     * @return -- the entity to be saved (additional validations could be performed in companion implementation) and re-fetched master entity
     */
    public static <
        MASTER_TYPE extends AbstractEntity<?>, 
        ITEM extends AbstractEntity<?>, 
        T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, 
        ID_TYPE
    > T2<T, MASTER_TYPE> validateAction(
        final T staleAction,
        final IEntityDao<T> companion,
        final Class<ID_TYPE> idType,
        final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller
    ) {
        final Result res = staleAction.isValid();
        // throw validation result of the action if it is not successful:
        if (!res.isSuccessful()) {
            throw res;
        }
        
        final MASTER_TYPE staleMasterEntity = controller.getMasterEntityFromAction(staleAction);
        final MASTER_TYPE refetchedMasterEntity = validateMasterEntityAndRefetch(staleMasterEntity, staleAction, Optional.empty(), controller);
        
        final T freshEntity;
        final Optional<Collection<ITEM>> freshAvailableItems;
        if (!staleAction.isPersistent()) {
            freshEntity = null;
            freshAvailableItems = Optional.empty();
        } else if (staleMasterEntity.getId() == null) {
            freshEntity = null;
            freshAvailableItems = Optional.of(controller.refetchAvailableItems(staleMasterEntity));
        } else {
            freshEntity = controller.refetchActionEntity(staleMasterEntity);
            freshAvailableItems = Optional.of(controller.refetchAvailableItems(staleMasterEntity));
        }
        if (freshAvailableItems.isPresent()) {
            final Map<Object, ITEM> availableEntities = mapById(freshAvailableItems.get(), idType);
            
            for (final ID_TYPE addedId : staleAction.getAddedIds()) {
                if (!availableEntities.containsKey(addedId)) {
                    throw Result.failure("Another user has deleted the item, that you're trying to choose. " + TRY_AGAIN_MSG);
                }
            }
            for (final ID_TYPE removedId : staleAction.getRemovedIds()) {
                if (!availableEntities.containsKey(removedId)) {
                    throw Result.failure("Another user has deleted the item, that you're trying to un-tick. " + TRY_AGAIN_MSG);
                }
            }
        }
        
        final T entityToSave;
        if (freshEntity != null) {
            if (freshEntity.getVersion() > staleAction.getSurrogateVersion()) {
                throw Result.failure("Another user has changed the chosen items. " + TRY_AGAIN_MSG);
            }
            entityToSave = freshEntity;
            staleAction.copyTo(entityToSave); // the main purpose of this copying is to promote addedIds, removedIds, chosenIds and 'availableEntities' property values further to entityToSave
            // mark entityToSave as 'dirty' to be properly saved and to increase its db-related version. New entity (persistedEntity == null) is always dirty - no need to do anything.
            entityToSave.setSurrogateVersion(freshEntity.getVersion() + 1L);
        } else {
            entityToSave = companion.new_();
            staleAction.copyTo(entityToSave); // the main purpose of this copying is to promote addedIds, removedIds, chosenIds and 'availableEntities' property values further to entityToSave
            if (!staleAction.getProperty(AbstractEntity.KEY).isRequired()) {
                // Key property, which represents id of master entity, will be null in case where master entity is new.
                // There is a need to relax key requiredness to be able to continue with action saving.
                entityToSave.getProperty(AbstractEntity.KEY).setRequired(false);
            }
            entityToSave.setKey(refetchedMasterEntity.getId());
            entityToSave.setSurrogateVersion(0L);
        }
        
        if (freshAvailableItems.isPresent()) {
            controller.setAvailableItems(entityToSave, freshAvailableItems.get());
        }

        return T2.t2(entityToSave, refetchedMasterEntity);
    }
    
    /**
     * Validates master entity on a) existence b) dirtiness c) disappearance from database. Returns fresh re-fetched instance.
     * 
     * @param masterEntityFromContext
     * 
     * @return fresh re-fetched master entity
     */
    private static <
        MASTER_TYPE extends AbstractEntity<?>, 
        ITEM extends AbstractEntity<?>, 
        T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, 
        ID_TYPE
    > MASTER_TYPE validateMasterEntityAndRefetch(
        final MASTER_TYPE masterEntityFromContext, 
        final T entity,
        final Optional<CentreContext<MASTER_TYPE, AbstractEntity<?>>> entityContext,
        final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller
    ) {
        if (masterEntityFromContext == null) {
            throw Result.failure("The master entity for collection modification is not provided in the context.");
        }
        if (!controller.skipDirtyChecking(entity, entityContext) && masterEntityFromContext.isInstrumented() && masterEntityFromContext.isDirty()) {
            throw Result.failure("This action is applicable only to a saved entity. Please save entity and try again.");
        }
        final MASTER_TYPE refetchedMasterEntity = controller.refetchMasterEntity(masterEntityFromContext);
        if (refetchedMasterEntity == null) {
            throw Result.failure("The master entity has been deleted. " + TRY_AGAIN_MSG);
        }
        return refetchedMasterEntity;
    }
    
    /**
     * Initialises entity from context and performs validations.
     * <p>
     * Validation includes full validation of master entity (existence, dirtiness and disappearance).
     * 
     * @param entity
     * @param context
     * @param controller
     * @return
     */
    public static <
        MASTER_TYPE extends AbstractEntity<?>, 
        ITEM extends AbstractEntity<?>, 
        T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, 
        ID_TYPE
    > T2<T, Optional<MASTER_TYPE>> initAction(
        final T entity,
        final CentreContext<MASTER_TYPE, AbstractEntity<?>> context,
        final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller
    ) {
        if (context == null) {
            return T2.t2(entity, Optional.empty()); // this is used for Cancel button (no context is needed)
        }
        
        final MASTER_TYPE masterEntityFromContext = controller.getMasterEntityFromContext(context);
        final MASTER_TYPE refetchedMasterEntity = validateMasterEntityAndRefetch(masterEntityFromContext, entity, Optional.of(context), controller);
        
        entity.setMasterEntity(masterEntityFromContext);
        
        // IMPORTANT: it is necessary to reset state for "key" property after its change.
        //   This is necessary to make the property marked as 'not changed from original' (origVal == val == 'DEMO') to be able not to re-apply afterwards
        //   the initial value against "key" property. Resetting will be done in DefaultEntityProducerWithContext.
        if (refetchedMasterEntity.getId() != null) {
            entity.setKey(refetchedMasterEntity.getId());
        } else {
            // Key property, which represents id of master entity, will be null in case where master entity is new.
            // There is a need to relax key requiredness to be able to continue with action saving.
            entity.getProperty(AbstractEntity.KEY).setRequired(false);
        }
        
        if (!entity.isPersistent() || refetchedMasterEntity.getId() == null) {
            entity.setSurrogateVersion(-1L);
        } else {
            entity.setSurrogateVersion(controller.persistedActionVersion(refetchedMasterEntity.getId()));
        }
        
        return T2.t2(entity, Optional.of(refetchedMasterEntity));
    }
    
    /**
     * Returns current version of existing (persisted) collection modification action for concrete <code>masterEntityId</code>.
     * If there is no existing action in the database then -1L is returned.
     * 
     * @param masterEntityId
     * @param actionCompanion
     * @return
     */
    public static <M extends AbstractEntity<?>> Long persistedActionVersion(final Long masterEntityId, final IEntityReader<M> actionCompanion) {
        return actionCompanion.findByKeyOptional(masterEntityId).map(action -> action.getVersion()).orElse(-1L);
    }
    
    /**
     * Returns the map between id and the entity with that id.
     * 
     * @param entities
     * @return
     */
    public static <T extends AbstractEntity<?>> Map<Object, T> mapById(final Collection<T> entities, final Class<?> idType) {
        final Map<Object, T> map = new HashMap<>();
        final boolean isLongId = Long.class.isAssignableFrom(idType);
        for (final T entity : entities) {
            if (isLongId) {
                map.put(entity.getId(), entity);
            } else {
                map.put(entity.getKey(), entity);
            }
        }
        return map;
    }
    
}
