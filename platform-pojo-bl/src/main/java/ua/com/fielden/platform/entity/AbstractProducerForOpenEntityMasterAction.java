package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntityEditActionProducer.NOTHING_TO_OPEN_MSG;

import java.util.function.Supplier;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.CompoundMasterException;

/**
 * A base class that should be applicable in most cases for implementing open entity master action producers.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <A>
 */
public abstract class AbstractProducerForOpenEntityMasterAction<T extends AbstractEntity<?>, A extends AbstractFunctionalEntityToOpenCompoundMaster<T>> extends DefaultEntityProducerWithContext<A> {
    private static final Supplier<? extends CompoundMasterException> NOTHING_TO_OPEN_EXCEPTION_SUPPLIER = () -> new CompoundMasterException(NOTHING_TO_OPEN_MSG);
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
        if (currentEntityNotEmpty()) {
            openAction.setKey(refetch(chosenEntityId(entityType).orElseThrow(NOTHING_TO_OPEN_EXCEPTION_SUPPLIER), entityType, KEY));
        } else if (selectedEntitiesEmpty()) {
            // '+' action on entity T centre or '+' action on master autocompleter title
            openAction.setKey(co(entityType).new_());
        } else {
            // it is recommended to throw 'unsupported case' exception otherwise
            throw new CompoundMasterException("Not supported yet.");
        }
        return openAction;
    }

}