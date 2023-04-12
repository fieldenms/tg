package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class EntityMasterHelpProducer extends DefaultEntityProducerWithContext<EntityMasterHelp> {

    @Inject
    public EntityMasterHelpProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityMasterHelp.class, companionFinder);
    }

    @Override
    protected EntityMasterHelp provideDefaultValues(final EntityMasterHelp entity) {
        if (masterEntityInstanceOf(OpenEntityMasterHelpAction.class)) {
            final OpenEntityMasterHelpAction masterEntity = masterEntity(OpenEntityMasterHelpAction.class);

            final EntityMasterHelpCo entityMasterHelpCo = co$(EntityMasterHelp.class);
            final EntityMasterHelp persistedEntity = entityMasterHelpCo.findByKeyAndFetch(entityMasterHelpCo.getFetchProvider().fetchModel(), masterEntity.getEntityType());

            if (persistedEntity != null) {
                return persistedEntity;
            } else {
                entity.setEntityType(masterEntity.getEntityType());
            }
        }
        return entity;
    }
}
