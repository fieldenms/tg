package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Provides default implementation for creation of {@link AbstractFunctionalEntityForCompoundMenuItem} instances.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class DefaultEntityProducerForCompoundMenuItem<T extends AbstractFunctionalEntityForCompoundMenuItem<AbstractEntity<?>> > extends DefaultEntityProducerWithContext<T> implements IEntityProducer<T> {
    
    public DefaultEntityProducerForCompoundMenuItem(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        final AbstractEntity<?> openCompoundMasterAction = getMasterEntity();
        
        if (openCompoundMasterAction == null) {
            throw new IllegalStateException("Upper level functional entity (for compound master opening) cannot be null.");
        }

        final AbstractEntity<?> compoundMasterKey = (AbstractEntity<?>) openCompoundMasterAction.getKey();
        entity.beginInitialising();
        entity.setKey(compoundMasterKey);
        entity.endInitialising();
        entity.resetMetaState();

        return entity;
    }
}
