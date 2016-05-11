package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

public class EntityExportActionProducer extends DefaultEntityProducerWithContext<EntityExportAction, AbstractEntity<?>> {

    @Inject
    public EntityExportActionProducer(final EntityFactory factory) {
        super(factory, EntityExportAction.class);
    }

    @Override
    protected EntityExportAction provideDefaultValues(final EntityExportAction entity) {
        entity.setAll(true);
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());
        }
        return entity;
    }
}
