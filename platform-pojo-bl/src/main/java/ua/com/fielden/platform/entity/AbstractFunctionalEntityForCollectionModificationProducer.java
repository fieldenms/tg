package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.initAction;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A base producer for {@link AbstractFunctionalEntityForCollectionModification} descendants.
 * <p>
 * Implementors should implement method {@link #provideCurrentlyAssociatedValues(AbstractFunctionalEntityForCollectionModification, AbstractEntity)} to initialise the properties
 * for this functional action.
 *
 * @author TG Team
 *
 */
public abstract class AbstractFunctionalEntityForCollectionModificationProducer<MASTER_TYPE extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, ID_TYPE, ITEM extends AbstractEntity<?>> extends DefaultEntityProducerWithContext<T> {
    
    @Inject
    public AbstractFunctionalEntityForCollectionModificationProducer(final EntityFactory factory, final Class<T> actionType, final ICompanionObjectFinder companionFinder) {
        super(factory, actionType, companionFinder);
    }
    
    /**
     * Returns {@link ICollectionModificationController} instance for this producer.
     * 
     * @return
     */
    abstract protected ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller();
    
    /**
     * Overridden to perform standard collection modification entity initialisation.
     */
    @Override
    protected final T provideDefaultValues(final T entity) {
        final T2<T, Optional<MASTER_TYPE>> actionAndMasterEntity = initAction(entity, (CentreContext<MASTER_TYPE, AbstractEntity<?>>) getContext(), controller());
        final T action = actionAndMasterEntity._1;
        return actionAndMasterEntity._2.map(refetchedMasterEntity -> provideCurrentlyAssociatedValues(action, refetchedMasterEntity)).orElse(action);
    }
    
    /**
     * Implement this method to initialise 1) 'chosenIds' 2) collection of all available entities.
     * <p>
     * 'chosenIds' property needs to be filled in with ids of those entities, that are present in master entity collection in concrete moment. The order of this ids might be
     * relevant or not, depending on the relevance of the order in master entity collection.
     * <p>
     * 'all available entities' property should be filled in with the fully-fledged entities (with their keys and descriptions etc), that can be chosen as collection items.
     * 
     * @param entity
     * @param refetchedMasterEntity
     * 
     * @return
     */
    protected abstract T provideCurrentlyAssociatedValues(final T entity, final MASTER_TYPE refetchedMasterEntity);
    
}