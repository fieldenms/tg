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
            if (isPersistentEntityType(currEntity.getType())) {
                final IEntityDao<AbstractEntity<?>> entityCo = (IEntityDao<AbstractEntity<?>>) co(currEntity.getType());
                final AbstractEntity<?> refetchedEntity = entityCo.findById(currEntity.getId(), fetchIdOnly(entityCo.getEntityType()).with("version", "createdBy", "createdDate", "lastUpdatedBy", "lastUpdatedDate"));
                entity.setEntityId(refetchedEntity.getId())
                        .setEntityVersion(entity.getVersion())
                        .setCreatedBy(entity.getCreatedBy())
                        .setCreatedDate(entity.getCreatedDate())
                        .setLastUpdatedBy(entity.getLastUpdatedBy())
                        .setLastUpdatedDate(entity.getLastUpdatedDate());
            }
        }
        return super.provideDefaultValues(entity);
    }
}
