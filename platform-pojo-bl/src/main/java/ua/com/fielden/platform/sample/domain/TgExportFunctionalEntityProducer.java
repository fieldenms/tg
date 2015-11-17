package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgExportFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgExportFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgExportFunctionalEntity, TgExportFunctionalEntity> implements IEntityProducer<TgExportFunctionalEntity> {

    @Inject
    public TgExportFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgExportFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgExportFunctionalEntity provideDefaultValues(final TgExportFunctionalEntity entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        
        if (getChosenProperty() != null) {
            entity.setActionProperty(getChosenProperty());
        }
        return entity;
    }
}