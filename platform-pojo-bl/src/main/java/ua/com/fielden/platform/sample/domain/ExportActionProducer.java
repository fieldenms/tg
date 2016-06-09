package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link ExportAction}.
 *
 * @author TG Team
 *
 */
public class ExportActionProducer extends DefaultEntityProducerWithContext<ExportAction> {

    @Inject
    public ExportActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, ExportAction.class, companionFinder);
    }

    @Override
    protected ExportAction provideDefaultValues(final ExportAction entity) {
        entity.setKey("EXPORT");
        entity.setCount(10);
        entity.resetMetaState();
        return entity;
    }
}