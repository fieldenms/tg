package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A base producer for {@link AbstractFunctionalEntityForCollectionModification} descendants.
 * <p>
 * Implementors should implement method {@link #provideCurrentlyAssociatedValues(AbstractFunctionalEntityForCollectionModification, AbstractEntity)} to initialise
 * the properties for this functional action.
 * <p>
 * This producer also initialises the master entity for this action. The master entity gets assigned into 'key'. To get properly fetched instance of
 * master entity there is a need to implement method {@link #fetchModelForMasterEntity()}. To specify, how to get the master entity from context, method {@link #getMasterEntityFromContext(CentreContext)} 
 * needs to be implemented.
 *
 * @author TG Team
 *
 */
public abstract class AbstractFunctionalEntityProducerForCollectionModification<MASTER_TYPE extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<MASTER_TYPE, ?>> extends DefaultEntityProducerWithContext<T, T> implements IEntityProducer<T> {
    private final IEntityDao<T> companion;
    private final ICompanionObjectFinder companionFinder;
    private final Class<MASTER_TYPE> masterEntityType;
    private static final String TRY_AGAIN_MSG = "Please cancel this action and try again!";
    
    @Inject
    public AbstractFunctionalEntityProducerForCollectionModification(final EntityFactory factory, final Class<T> actionType, final ICompanionObjectFinder companionFinder) {
        super(factory, actionType, companionFinder);
        this.masterEntityType = (Class<MASTER_TYPE>) PropertyTypeDeterminator.determinePropertyType(actionType, AbstractEntity.KEY);
        this.companion = companionFinder.find(actionType);
        this.companionFinder = companionFinder;
    }
    
    /**
     * Retrieves master entity from context. Need to implement this for concrete action. Most likely the master entity is <code>context.getCurrEntity()</code> or <code>context.getMasterEntity()</code>.
     * 
     * @return
     */
    protected abstract AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context);
    
    protected abstract fetch<MASTER_TYPE> fetchModelForMasterEntity();
    
    @Override
    protected final T provideDefaultValues(final T entity) {
        if (getCentreContext() == null) {
            return entity; // this is used for Cancel button (no context is needed)
        }
        
        entity.setContext(getCentreContext());

        final AbstractEntity<?> masterEntityFromContext = getMasterEntityFromContext(entity.getContext());
        if (masterEntityFromContext == null) {
            throw Result.failure("The master entity for collection modification is not provided in the context.");
        }
        if (masterEntityFromContext.isDirty()) {
            throw Result.failure("This action is applicable only to a saved entity! Please save entity and try again!");
        }
        final MASTER_TYPE masterEntity = companionFinder.find(masterEntityType).findById(masterEntityFromContext.getId(), fetchModelForMasterEntity());
        if (masterEntity == null) {
            throw Result.failure("The master entity has been deleted. " + TRY_AGAIN_MSG);
        }
        
        // IMPORTANT: it is necessary to reset state for "key" property after its change.
        //   This is necessary to make the property marked as 'not changed from original' (origVal == val == 'DEMO') to be able not to re-apply afterwards
        //   the initial value against "key" property
        entity.setKey(masterEntity);
        entity.getProperty(AbstractEntity.KEY).resetState();
        
        final T previouslyPersistedAction = retrieveActionFor(masterEntity, companion, entityType);
        
        // IMPORTANT: it is necessary not to reset state for "surrogateVersion" property after its change.
        //   This is necessary to leave the property marked as 'changed from original' (origVal == null) to be able to apply afterwards
        //   the initial value against '"surrogateVersion", that was possibly changed by another user'
        entity.setSurrogateVersion(surrogateVersion(previouslyPersistedAction));

        return provideCurrentlyAssociatedValues(entity, masterEntity);
    }
    
    /**
     * Implement this method to initialise 1) 'chosenIds' 2) collection of all available entities.
     * <p>
     * 'chosenIds' property needs to be filled in with ids of those entities, that are present in master entity collection in concrete moment. The order of this ids might be relevant or not,
     * depending on the relevance of the order in master entity collection.
     * <p>
     * 'all available entities' property should be filled in with the fully-fledged entities (with their keys and descriptions etc), that can be chosen as collection items.
     * 
     * @param entity
     * @param masterEntity
     * 
     * @return
     */
    protected abstract T provideCurrentlyAssociatedValues(final T entity, final MASTER_TYPE masterEntity);
    
    private static <T extends AbstractEntity<?>> Long surrogateVersion(final T persistedEntity) {
        return persistedEntity == null ? 99L : (persistedEntity.getVersion() + 100L);
    }
    
    private static <MASTER_TYPE extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<MASTER_TYPE, ?>> T retrieveActionFor(final MASTER_TYPE masterEntity, final IEntityDao<T> companion, final Class<T> actionType) {
        return companion.getEntity(
                from(select(actionType).where().prop(AbstractEntity.KEY).eq().val(masterEntity).model())
                .with(fetchAndInstrument(actionType).with(AbstractEntity.KEY)).model()
        );
    }
    
    /**
     * Validates restored action (it was "produced" by corresponding producer) on subject of 1) removal of available collectional items 2) concurrent modification of the same collection for the same master entity.
     * After the validation has 
     * 
     * @param action
     * @param availableEntitiesGetter
     * @param companion
     * @param factory
     * @return
     */
    public static <MASTER_TYPE extends AbstractEntity<?>, ITEM extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<MASTER_TYPE, ID_TYPE>, ID_TYPE> T validateAction(final T action, final Function<T, Collection<ITEM>> availableEntitiesGetter, final IEntityDao<T> companion, final EntityFactory factory, final Class<ID_TYPE> idType) {
        final Result res = action.isValid();
        // throw validation result of the action if it is not successful:
        if (!res.isSuccessful()) {
            throw res;
        }
        
        final MASTER_TYPE masterEntityBeingUpdated = action.getKey(); // existence of master entity is checked during "producing" of functional action
        
        final Map<Object, ITEM> availableEntities = mapById(availableEntitiesGetter.apply(action), idType);
        
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
        
        final Class<T> actionType = (Class<T>) action.getType();
        final T persistedEntity = retrieveActionFor(masterEntityBeingUpdated, companion, actionType);
        
        if (surrogateVersion(persistedEntity) > action.getSurrogateVersion()) {
            throw Result.failure("Another user has changed the chosen items. " + TRY_AGAIN_MSG);
        }
        
        final T entityToSave;
        // the next block of code is intended to mark entityToSave as 'dirty' to be properly saved and to increase its db-related version. New entity (persistedEntity == null) is always dirty - no need to do anything.
        if (persistedEntity != null) {
            entityToSave = persistedEntity;
            entityToSave.setSurrogateVersion(persistedEntity.getVersion() + 1L);
        } else {
            entityToSave = factory.newEntity(actionType, null);
            entityToSave.setKey(masterEntityBeingUpdated);
        }
        return entityToSave;
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