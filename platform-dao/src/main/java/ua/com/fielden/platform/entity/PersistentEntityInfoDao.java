package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentWithAuditData;

/// DAO implementation for companion object {@link PersistentEntityInfoCo}.
///
@EntityType(PersistentEntityInfo.class)
public class PersistentEntityInfoDao extends CommonEntityDao<PersistentEntityInfo> implements PersistentEntityInfoCo {

    public static final String ERR_NOT_SUITABLE_ENTITY = "Current entity [%s] does not have the versioning info.";

    @Override
    public PersistentEntityInfo initEntityWith(final AbstractEntity persistentEntity, PersistentEntityInfo entity) {
        if (isPersistentWithAuditData(persistentEntity.getType())) {
            final var entityCo = (IEntityDao<AbstractPersistentEntity<?>>) co(persistentEntity.getType());
            final var entityWithInfo = entityCo.findById(persistentEntity.getId(), fetchKeyAndDescOnly(entityCo.getEntityType())
                    .with(AbstractPersistentEntity.VERSION)
                    .with(AbstractPersistentEntity.CREATED_BY)
                    .with(AbstractPersistentEntity.CREATED_DATE)
                    .with(AbstractPersistentEntity.LAST_UPDATED_BY)
                    .with(AbstractPersistentEntity.LAST_UPDATED_DATE));
            entity.setEntityId(entityWithInfo.getId())
                    .setEntityType(entityCo.getEntityType().getName())
                    .setEntityVersion(entityWithInfo.getVersion())
                    .setCreatedBy(entityWithInfo.getCreatedBy())
                    .setCreatedDate(entityWithInfo.getCreatedDate())
                    .setLastUpdatedBy(entityWithInfo.getLastUpdatedBy())
                    .setLastUpdatedDate(entityWithInfo.getLastUpdatedDate())
                    .setEntityTitle(isEmpty(entityWithInfo.getDesc()) ?
                            "%s".formatted(entityWithInfo.getKey()) :
                            "%s: %s".formatted(entityWithInfo.getKey(), entityWithInfo.getDesc()));
            return entity;
        } else {
            throw new InvalidStateException(ERR_NOT_SUITABLE_ENTITY.formatted(persistentEntity.getType().getSimpleName()));
        }
    }
}
