package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgExportFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgCreatePersistentStatusActionProducer extends DefaultEntityProducerWithContext<TgCreatePersistentStatusAction, TgCreatePersistentStatusAction> implements IEntityProducer<TgCreatePersistentStatusAction> {

    @Inject
    public TgCreatePersistentStatusActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgCreatePersistentStatusAction.class, companionFinder);
    }

    @Override
    protected TgCreatePersistentStatusAction provideDefaultValues(final TgCreatePersistentStatusAction entity) {
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
            final TgPersistentEntityWithProperties me = (TgPersistentEntityWithProperties) entity.getContext().getMasterEntity();
            if (me.isDirty()) {
                throw Result.failure("This action is applicable only to a saved entity! Please save entity and try again!");
            }
        }
        
        if (getChosenProperty() != null) {
            entity.setActionProperty(getChosenProperty());
        }
        
        return entity;
    }
}