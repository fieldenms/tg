package ua.com.fielden.platform.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
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
     * Validates restored action (it was "produced" by corresponding producer) on subject of 1) removal of available collectional items 2) concurrent modification of the same
     * collection for the same master entity. After the validation has
     * 
     * @param action
     * @param companion
     * @param factory
     * @return
     */
    public static <
        MASTER_TYPE extends AbstractEntity<?>, 
        ITEM extends AbstractEntity<?>, 
        T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, 
        ID_TYPE
    > T2<T, MASTER_TYPE> validateAction(
        final T action,
        final IEntityDao<T> companion,
        final EntityFactory factory,
        final Class<ID_TYPE> idType,
        final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller
    ) {
        final Result res = action.isValid();
        // throw validation result of the action if it is not successful:
        if (!res.isSuccessful()) {
            throw res;
        }

        // TODO remove final MASTER_TYPE masterEntityBeingUpdated = (MASTER_TYPE) action.refetchedMasterEntity(); // existence of master entity is checked during "producing" of functional action
        final MASTER_TYPE masterEntityBeingUpdated = validateMasterEntityAndRefetch(controller.getMasterEntityFromAction(action), action, Optional.empty(), controller);
        final Optional<T2<T, Collection<ITEM>>> persistedEntityAndAvailableItemsOption = retrieveActionFor(masterEntityBeingUpdated, action.isPersistent(), controller);
        
        final T persistedEntity = persistedEntityAndAvailableItemsOption.map(persistedActionAndAvailableItems -> {
            final Map<Object, ITEM> availableEntities = mapById(persistedActionAndAvailableItems._2, idType);

            for (final ID_TYPE addedId : action.getAddedIds()) {
                if (!availableEntities.containsKey(addedId)) {
                    throw Result.failure("Another user has deleted the item, that you're trying to choose. " + TRY_AGAIN_MSG);
                }
            }
            for (final ID_TYPE removedId : action.getRemovedIds()) {
                if (!availableEntities.containsKey(removedId)) {
                    throw Result.failure("Another user has deleted the item, that you're trying to un-tick. " + TRY_AGAIN_MSG);
                }
            }
            return persistedActionAndAvailableItems._1;
        }).orElse(null);
        
        if (surrogateVersion(persistedEntity) > action.getSurrogateVersion()) {
            throw Result.failure("Another user has changed the chosen items. " + TRY_AGAIN_MSG);
        }

        final T entityToSave;
        // the next block of code is intended to mark entityToSave as 'dirty' to be properly saved and to increase its db-related version. New entity (persistedEntity == null) is always dirty - no need to do anything.
        if (persistedEntity != null) {
            entityToSave = persistedEntity;
            action.copyTo(entityToSave); // the main purpose of this copying is to promote addedIds, removedIds, chosenIds and 'availableEntities' property values further to entityToSave
            entityToSave.setSurrogateVersion(persistedEntity.getVersion() + 1L);
        } else {
            entityToSave = companion.new_();
            action.copyTo(entityToSave); // the main purpose of this copying is to promote addedIds, removedIds, chosenIds and 'availableEntities' property values further to entityToSave
            if (!action.getProperty(AbstractEntity.KEY).isRequired()) {
                // Key property, which represents id of master entity, will be null in case where master entity is new.
                // There is a need to relax key requiredness to be able to continue with action saving.
                entityToSave.getProperty(AbstractEntity.KEY).setRequired(false);
            }
            entityToSave.setKey(masterEntityBeingUpdated.getId());
            entityToSave.setSurrogateVersion(null);
        }
        // TODO consider passing refetched master entity further if needed: entityToSave.setRefetchedMasterEntity(masterEntityBeingUpdated);

        return T2.t2(entityToSave, masterEntityBeingUpdated);
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
        final AbstractEntity<?> masterEntityFromContext, 
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
        
        final AbstractEntity<?> masterEntityFromContext = controller.getMasterEntityFromContext(context);
        final MASTER_TYPE refetchedMasterEntity = validateMasterEntityAndRefetch(masterEntityFromContext, entity, Optional.of(context), controller);
        
        if (masterEntityFromContext instanceof EnhancedCentreEntityQueryCriteria) {
            entity.setMasterEntityHolder(((EnhancedCentreEntityQueryCriteria) masterEntityFromContext).centreContextHolder());
        } else {
            entity.setMasterEntity(masterEntityFromContext);
        }
        
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
        
        final T previouslyPersistedAction = retrieveActionFor(refetchedMasterEntity, entity.isPersistent(), controller).map(actionAndAvailableItems -> actionAndAvailableItems._1).orElse(null);
        entity.setSurrogateVersion(surrogateVersion(previouslyPersistedAction));
        return T2.t2(entity, Optional.of(refetchedMasterEntity));
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
    
    private static <T extends AbstractEntity<?>> Long surrogateVersion(final T persistedEntity) {
        return persistedEntity == null ? 99L : (persistedEntity.getVersion() + 100L);
    }
    
    private static <
        MASTER_TYPE extends AbstractEntity<?>, 
        ITEM extends AbstractEntity<?>, 
        T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, 
        ID_TYPE
    > Optional<T2<T, Collection<ITEM>>> retrieveActionFor(
        final MASTER_TYPE masterEntity,
        final boolean isActionEntityPersistent,
        final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller
    ) {
        if (!isActionEntityPersistent || masterEntity.getId() == null) {
            return Optional.empty();
        }
        return Optional.of(controller.refetchActionEntity(masterEntity));
    }
    
}
