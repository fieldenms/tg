package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.Optional;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A base class that should be applicable in most cases for implementing open entity master action producers.
 * 
 * @author TG Air Team
 *
 * @param <T>
 * @param <A>
 */
public abstract class AbstractProducerForOpenEntityMasterAction<T extends AbstractEntity<?>, A extends AbstractFunctionalEntityToOpenCompoundMaster<T>> extends DefaultEntityProducerWithContext<A> {

    protected final Class<T> entityType;
    
    public AbstractProducerForOpenEntityMasterAction(
            final EntityFactory factory,
            final Class<T> entityType,
            final Class<A> openActionType,            
            final ICompanionObjectFinder companionFinder) {
        super(factory, openActionType, companionFinder);
        this.entityType = entityType;
    }
    
    @Override
    protected A provideDefaultValues(final A openAction) {
        if (currentEntityInstanceOf(entityType) && chosenPropertyRepresentsThisColumn()) {
            // edit current T row by clicking 'this' column on entity T centre
            openAction.setKey(refetch(currentEntity(entityType), KEY));
        } else if (currentEntityInstanceOf(entityType) && chosenPropertyEmpty()) {
            // primary action on entity T centre (edit entity)
            openAction.setKey(refetch(currentEntity(entityType), KEY));
        } else if (currentEntityNotEmpty() && chosenPropertyNotEmpty()) {
            // there are two possible legitimate cases here:
            // 1. either currentEntity().get(chosenProperty()) is of type T and all is good, or
            // 2. chosenProperty is a sub property of a property of type T, where that "parent" property belongs to the current entity, or
            // 3. we have a genuine bug and need to throw an appropriate error

            final Optional<T> optClickedEntity = EntityUtils.traversePropPath(currentEntity(), chosenProperty())
                    .filter(t2 -> t2._2.map(v -> entityType.isAssignableFrom(v.getType())).orElse(false)) // find only type-compatible values on path
                    .map(t2 -> entityType.cast(t2._2.get())).findFirst();
            openAction.setKey(refetch(optClickedEntity.orElseThrow(() -> new CompoundMasterException("There is no entity to open.")), KEY));
        } else if (masterEntityEmpty() && selectedEntitiesEmpty()) {
            // '+' action on entity T centre
            openAction.setKey(co(entityType).new_());
        } else {
            // it is recommended to throw 'unsupported case' exception otherwise
            throw new CompoundMasterException("Not supported yet.");
        }
        return openAction;
    }


}
