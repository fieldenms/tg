package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.utils.EntityUtils;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;

public class PersistentEntityInfoProducer extends DefaultEntityProducerWithContext<PersistentEntityInfo> {

    @Inject
    public PersistentEntityInfoProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, PersistentEntityInfo.class, companionFinder);
    }

    @Override
    protected PersistentEntityInfo provideDefaultValues(final PersistentEntityInfo entity) {
        if (currentEntityNotEmpty()) {
            final AbstractEntity<?> currEntity = currentEntity();
            if (isPersistentEntityType(currEntity.getType()) && AbstractPersistentEntity.class.isAssignableFrom(currEntity.getType())) {
                final IEntityDao<AbstractPersistentEntity<?>> entityCo = (IEntityDao<AbstractPersistentEntity<?>>) co(currEntity.getType());
                final AbstractPersistentEntity<?> refetchedEntity = entityCo.findById(currEntity.getId(), fetchIdOnly(entityCo.getEntityType()).with("version", "createdBy", "createdDate", "lastUpdatedBy", "lastUpdatedDate"));
                entity.setEntityId(refetchedEntity.getId())
                        .setEntityVersion(refetchedEntity.getVersion())
                        .setCreatedBy(refetchedEntity.getCreatedBy())
                        .setCreatedDate(refetchedEntity.getCreatedDate())
                        .setLastUpdatedBy(refetchedEntity.getLastUpdatedBy())
                        .setLastUpdatedDate(refetchedEntity.getLastUpdatedDate());
            }
        }
        return super.provideDefaultValues(entity);
    }
}
