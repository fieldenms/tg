package ua.com.fielden.platform.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * An interface for controlling how to interact with collection modification functional entity.
 * 
 * @author TG Team
 *
 */
public interface ICollectionModificationController<MASTER_TYPE extends AbstractEntity<?>, T extends AbstractFunctionalEntityForCollectionModification<ID_TYPE>, ID_TYPE, ITEM extends AbstractEntity<?>> {
    
    /**
     * Retrieves master entity from context. Need to implement this for concrete action. Most likely the master entity is <code>context.getCurrEntity()</code> or
     * <code>context.getMasterEntity()</code>.
     * 
     * TODO return type MASTER_TYPE?
     * 
     * @return
     */
    AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context);
    
    /**
     * Retrieves master entity from action entity.
     * 
     * TODO some caching?
     * TODO return type MASTER_TYPE?
     * 
     * @return
     */
    default AbstractEntity<?> getMasterEntityFromAction(final T action) {
        return action.getMasterEntity();
    }
    
//    default fetch<MASTER_TYPE> fetchModelForMasterEntity() {
//        throw new CollectionModificationException("Method 'fetchModelForMasterEntity' is not implemented.");
//    }
    
    MASTER_TYPE refetchMasterEntity(final AbstractEntity<?> masterEntityFromContext);
    
    default T2<T, Collection<ITEM>> refetchActionEntity(final Long masterEntityId) {
        throw new CollectionModificationException("Unsupported.");
    }
//    {
//        return companionFinder.find((Class<MASTER_TYPE>) masterEntityFromContext.getDerivedFromType()).findById(masterEntityFromContext.getId(), fetchModelForMasterEntity());
//    }
    
    /**
     * By default, collection modification is prohibited in case of dirty (persisted and changed, new) entity. However, there are edge-cases where collection modification is a part
     * of master entity saving process through the use of continuation. In those cases the master entity will be dirty and the check on dirtiness should be relaxed.
     * <p>
     * This method provides such point of customization.
     * 
     * @param action
     *            -- collection modification functional entity with its context (which contain in general case computation part to be able to differentiate functional action
     *            origin, for example a) master property action b) continuation etc.)
     * @return
     */
    default boolean skipDirtyChecking(final T action) {
        return false;
    }
    
    /**
     * Additional properties to be skipped for meta-state resetting for collection modification functional entity. 'surrogateVersion' property will be skipped automatically -- no
     * need to be listed here.
     * 
     * @return
     */
    default List<String> skipPropertiesForMetaStateResettingInCollectionalEditor() {
        return Arrays.asList();
    }
    
}
