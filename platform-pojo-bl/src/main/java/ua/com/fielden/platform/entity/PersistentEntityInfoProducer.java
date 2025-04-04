package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.functional.PersistentEntityInfo_CanExecute_Token;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentWithAuditData;

public class PersistentEntityInfoProducer extends DefaultEntityProducerWithContext<PersistentEntityInfo> {

    @Inject
    public PersistentEntityInfoProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, PersistentEntityInfo.class, companionFinder);
    }

    @Override
    @Authorise(PersistentEntityInfo_CanExecute_Token.class)
    protected PersistentEntityInfo provideDefaultValues(final PersistentEntityInfo entity) {
        if (currentEntityNotEmpty()) {
            final AbstractEntity<?> currEntity = currentEntity();
            if (isPersistentWithAuditData(currEntity.getType())) {
                final IEntityDao<AbstractPersistentEntity<?>> entityCo = (IEntityDao<AbstractPersistentEntity<?>>) co(currEntity.getType());
                final AbstractPersistentEntity<?> refetchedEntity = entityCo.findById(currEntity.getId(), fetchKeyAndDescOnly(entityCo.getEntityType()).with("version", "createdBy", "createdDate", "lastUpdatedBy", "lastUpdatedDate"));
                entity.setEntityId(refetchedEntity.getId())
                        .setEntityVersion(refetchedEntity.getVersion())
                        .setCreatedBy(refetchedEntity.getCreatedBy())
                        .setCreatedDate(refetchedEntity.getCreatedDate())
                        .setLastUpdatedBy(refetchedEntity.getLastUpdatedBy())
                        .setLastUpdatedDate(refetchedEntity.getLastUpdatedDate())
                        .setEntityTitle(isEmpty(refetchedEntity.getDesc()) ? format("%s", refetchedEntity.getKey()) : format("%s: %s", refetchedEntity.getKey(), refetchedEntity.getDesc()));
            }
        }
        return super.provideDefaultValues(entity);
    }
}
