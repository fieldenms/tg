package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A base producer for {@link AbstractFunctionalEntityForCollectionModification} descendants.
 * <p>
 * Implementors should implement method {@link #provideCurrentlyAssociatedValues(AbstractFunctionalEntityForCollectionModification, AbstractEntity)} to initialise the properties
 * for this functional action.
 * <p>
 * This producer also initialises the master entity for this action. The master entity gets assigned into 'key'. To get properly fetched instance of master entity there is a need
 * to implement method {@link #fetchModelForMasterEntity()}. To specify, how to get the master entity from context, method {@link #getMasterEntityFromContext(CentreContext)} needs
 * to be implemented.
 *
 * @author TG Team
 *
 */
public abstract class AbstractFunctionalEntityForCollectionModificationProducer<MASTER_TYPE extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, ID_TYPE, ITEM extends AbstractEntity<?>> extends DefaultEntityProducerWithContext<T> implements IEntityProducer<T> {
    
    @Inject
    public AbstractFunctionalEntityForCollectionModificationProducer(final EntityFactory factory, final Class<T> actionType, final ICompanionObjectFinder companionFinder) {
        super(factory, actionType, companionFinder);
    }
    
    abstract protected ICollectionModificationController<MASTER_TYPE, T, ID_TYPE, ITEM> controller();
    
    @Override
    protected final T provideDefaultValues(final T entity) {
        final CentreContext<MASTER_TYPE, AbstractEntity<?>> context = (CentreContext<MASTER_TYPE, AbstractEntity<?>>) getContext();
        final T2<T, Optional<MASTER_TYPE>> actionAndMasterEntity = CollectionModificationUtils.initAction(entity, context, controller());
        final T action = actionAndMasterEntity._1;
        return actionAndMasterEntity._2.map(refetchedMasterEntity -> provideCurrentlyAssociatedValues(action, refetchedMasterEntity)).orElse(action);
    }
    
    /**
     * IMPORTANT: it is necessary NOT to reset state for "surrogateVersion" property after its change. This is necessary to leave the property marked as 'changed from original'
     * (origVal == null) to be able to apply afterwards the initial value against '"surrogateVersion", that was possibly changed by another user'.
     * 
     * @return
     */
    @Override
    protected final List<String> skipPropertiesForMetaStateResetting() {
        final List<String> propertiesToBeSkipped = new ArrayList<>();
        propertiesToBeSkipped.add("surrogateVersion");
        propertiesToBeSkipped.addAll(skipPropertiesForMetaStateResettingInCollectionalEditor());
        return propertiesToBeSkipped;
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
    
    /**
     * Additional properties to be skipped for meta-state resetting for collection modification functional entity. 'surrogateVersion' property will be skipped automatically -- no
     * need to be listed here.
     * 
     * @return
     */
    protected List<String> skipPropertiesForMetaStateResettingInCollectionalEditor() {
        return Arrays.asList();
    }
    
}