package ua.com.fielden.platform.entity;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * @param staleAction -- stale instance of action with non-stale addedIds, removedIds and chosenIds. Please note that available items could be stale. Use returning action with updated available items instead.
     * @param companion
     * @param idType
     * @param controller
     * @return -- the action to be saved (additional validations could be performed in companion implementation) and re-fetched master entity
     */
    public static <MASTER_TYPE extends AbstractEntity<?>, ITEM extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, ID_TYPE> 
        T2<T, MASTER_TYPE> validateAction(
            final T staleAction,
            final IEntityDao<T> companion,
            final Class<ID_TYPE> idType,
            final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller) {
        final Result res = staleAction.isValid();
        // throw validation result of the action if it is not successful:
        if (!res.isSuccessful()) {
            throw res;
        }
        
        final MASTER_TYPE staleMasterEntity = controller.getMasterEntityFromAction(staleAction);
        final MASTER_TYPE refetchedMasterEntity = validateMasterEntityAndRefetch(staleMasterEntity, staleAction, empty(), controller);
        
        final T freshEntity;
        final Optional<Collection<ITEM>> freshAvailableItems;
        if (!staleAction.isPersistent()) {
            freshEntity = null;
            freshAvailableItems = empty();
        } else if (staleMasterEntity.getId() == null) {
            freshEntity = null;
            // Available items should be re-fetched in both cases: persisted and non-persisted master entity.
            freshAvailableItems = of(controller.refetchAvailableItems(staleMasterEntity));
        } else {
            // Persisted action could exist only in case of persisted master entity. But it could be 'null', this is the case of action history beginning.
            freshEntity = controller.refetchActionEntity(staleMasterEntity);
            freshAvailableItems = of(controller.refetchAvailableItems(staleMasterEntity));
        }
        if (freshAvailableItems.isPresent()) {
            // There is a need to validate existence of available items that was checked or unchecked: if such item disappears then validation conflict should be thrown.
            final boolean isLongId = Long.class.isAssignableFrom(idType);
            final Map<Object, ITEM> availableEntities = toMap(freshAvailableItems.get(), ent -> isLongId ? ent.getId() : ent.getKey());
            
            for (final ID_TYPE addedId : staleAction.getAddedIds()) {
                if (!availableEntities.containsKey(addedId)) {
                    throw failure("Another user has deleted the item, that you're trying to choose. " + TRY_AGAIN_MSG);
                }
            }
            for (final ID_TYPE removedId : staleAction.getRemovedIds()) {
                if (!availableEntities.containsKey(removedId)) {
                    throw failure("Another user has deleted the item, that you're trying to un-tick. " + TRY_AGAIN_MSG);
                }
            }
        }
        
        final T entityToSave;
        if (freshEntity != null) {
            // Stale action's surrogate version can be either -1 (no persisted action existed during producing phase) or >= 0 (version of persisted action existed during producing phase).
            // If freshEntity is not empty then it represents new version of persisted action. It should be validated on conflict:
            if (freshEntity.getVersion() > staleAction.getSurrogateVersion()) {
                throw failure("Another user has changed the chosen items. " + TRY_AGAIN_MSG);
            }
            entityToSave = freshEntity;
            staleAction.copyTo(entityToSave); // the main purpose of this copying is to promote addedIds, removedIds, chosenIds and 'availableEntities' property values further to entityToSave
            // mark entityToSave as 'dirty' to be properly saved and to increase its db-related version. New entity (persistedEntity == null) is always dirty - no need to do anything.
            entityToSave.setSurrogateVersion(freshEntity.getVersion() + 1L); // surrogate version will be equal to actual action version after saving
        } else {
            entityToSave = companion.new_();
            staleAction.copyTo(entityToSave); // the main purpose of this copying is to promote addedIds, removedIds, chosenIds and 'availableEntities' property values further to entityToSave
            if (!staleAction.getProperty(KEY).isRequired()) {
                // Key property, which represents id of master entity, will be null in case where master entity is new.
                // There is a need to relax key requiredness to be able to continue with action saving.
                entityToSave.getProperty(KEY).setRequired(false);
            }
            entityToSave.setKey(refetchedMasterEntity.getId()); // here id could be null and it is legitimate situation indicating that master entity is new. This makes the action possible to being used as continuation.
            entityToSave.setSurrogateVersion(0L); // surrogate version will be equal to actual action version after saving
        }
        
        if (freshAvailableItems.isPresent()) {
            // need to override 'stale' available items with fresh ones: this is needed in case of additional custom validations in concrete companion objects
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
    private static <MASTER_TYPE extends AbstractEntity<?>, ITEM extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, ID_TYPE> 
        MASTER_TYPE validateMasterEntityAndRefetch(
            final MASTER_TYPE masterEntityFromContext, 
            final T entity,
            final Optional<CentreContext<MASTER_TYPE, AbstractEntity<?>>> entityContext,
            final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller) {
        if (masterEntityFromContext == null) {
            throw failure("The master entity for collection modification is not provided in the context.");
        }
        if (!controller.skipDirtyChecking(entity, entityContext) && masterEntityFromContext.isInstrumented() && masterEntityFromContext.isDirty()) {
            throw failure("This action is applicable only to a saved entity. Please save entity and try again.");
        }
        final MASTER_TYPE refetchedMasterEntity = controller.refetchMasterEntity(masterEntityFromContext);
        if (refetchedMasterEntity == null) {
            throw failure("The master entity has been deleted. " + TRY_AGAIN_MSG);
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
     * @return -- a pair of initialised action with 1) re-fetched master entity or 2) empty optional in the case of 'Cancel' button pressed
     */
    public static <MASTER_TYPE extends AbstractEntity<?>, ITEM extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, ID_TYPE> 
        T2<T, Optional<MASTER_TYPE>> initAction(
            final T entity,
            final CentreContext<MASTER_TYPE, AbstractEntity<?>> context,
            final ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller) {
        if (context == null) {
            return T2.t2(entity, empty()); // this is used for Cancel button (no context is needed)
        }
        
        final MASTER_TYPE masterEntityFromContext = controller.getMasterEntityFromContext(context);
        final MASTER_TYPE refetchedMasterEntity = validateMasterEntityAndRefetch(masterEntityFromContext, entity, of(context), controller);
        // The master entity is being set here to be able to retrieve it later in companion object's logic.
        // Please note that we need exact original master entity instance, this is especially important for dirty instances (continuation case) which become non-dirty after re-fetching.
        entity.setMasterEntity(masterEntityFromContext);
        
        // IMPORTANT: it is necessary to reset state for "key" property after its change.
        //   This is necessary to make the property marked as 'not changed from original' (origVal == val == 'DEMO') to be able not to re-apply afterwards
        //   the initial value against "key" property. Resetting will be done in DefaultEntityProducerWithContext.
        if (refetchedMasterEntity.getId() != null) {
            entity.setKey(refetchedMasterEntity.getId());
        } else {
            // Key property, which represents id of master entity, will be null in case where master entity is new.
            // There is a need to relax key requiredness to be able to continue with action saving.
            entity.getProperty(KEY).setRequired(false);
        }
        
        if (!entity.isPersistent() || refetchedMasterEntity.getId() == null) {
            entity.setSurrogateVersion(-1L); // persisted action does not exist
        } else {
            entity.setSurrogateVersion(controller.persistedActionVersion(refetchedMasterEntity.getId()));
        }
        
        return t2(entity, of(refetchedMasterEntity));
    }
    
    /**
     * Returns current version of existing (persisted) collection modification action for concrete <code>masterEntityId</code>.
     * If there is no existing action in the database then -1L is returned.
     * 
     * @param masterEntityId
     * @param actionCompanion
     * @return
     */
    public static <M extends AbstractEntity<?>> Long persistedActionVersionFor(final Long masterEntityId, final IEntityReader<M> actionCompanion) {
        return actionCompanion.findByKeyOptional(masterEntityId).map(action -> action.getVersion()).orElse(-1L);
    }
    
    /**
     * Converts a collection of entities to a map between IDs and corresponding entities.
     * 
     * @param entities
     * @return
     */
    public static <T extends AbstractEntity<?>> Map<Object, T> toMapById(final Collection<T> entities) {
        return toMap(entities, ent -> ent.getId());
    }


    /**
     * Converts a collection of entities to a map between KEYs and corresponding entities.
     * 
     * @param entities
     * @return
     */
    public static <T extends AbstractEntity<?>> Map<Object, T> toMapByKey(final Collection<T> entities) {
        return toMap(entities, ent -> ent.getKey());
    }
    
    /**
     * Converts a collection of entities to a map between identifiers as defined by function <code>identifier</code> and corresponding entities.
     * 
     * @param entities
     * @param keyMapper -- a function that maps an entity to a corresponding key-value in the resultant map.
     * @return
     */
    public static <T extends AbstractEntity<?>> Map<Object, T> toMap(final Collection<T> entities, final Function<T, Object> keyMapper) {
        return entities.stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }
}
