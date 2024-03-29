package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgExportFunctionalEntity}.
 *
 * @author TG Team
 *
 */
public class TgExportFunctionalEntityProducer extends DefaultEntityProducerWithContext<TgExportFunctionalEntity> {

    @Inject
    public TgExportFunctionalEntityProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgExportFunctionalEntity.class, companionFinder);
    }

    @Override
    protected TgExportFunctionalEntity provideDefaultValues(final TgExportFunctionalEntity entity) {
        if (masterEntityNotEmpty()) {
            entity.setMasterEntity(masterEntity(TgPersistentEntityWithProperties.class));
        }
        if (chosenPropertyNotEmpty()) {
            entity.setActionProperty(chosenProperty());
        }
        return entity;
    }
}