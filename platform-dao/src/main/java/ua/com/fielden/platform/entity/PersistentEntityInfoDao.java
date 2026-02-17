package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentWithVersionData;

/// DAO implementation for the {@link PersistentEntityInfoCo} companion object .
///
@EntityType(PersistentEntityInfo.class)
public class PersistentEntityInfoDao extends CommonEntityDao<PersistentEntityInfo> implements PersistentEntityInfoCo {

    public static final String ERR_NOT_SUITABLE_ENTITY = "Entity [%s] does not have version information.";

    @SuppressWarnings("unchecked")
    @Override
    public PersistentEntityInfo initialise(final AbstractEntity<?> entity, PersistentEntityInfo info) {
        if (isPersistentWithVersionData(entity.getType())) {
            final var entityCo = co((Class<AbstractPersistentEntity<?>>) entity.getType());
            final var entityFetch = fetchKeyAndDescOnly(entityCo.getEntityType()).with(VERSION, CREATED_BY, CREATED_DATE, LAST_UPDATED_BY, LAST_UPDATED_DATE);
            final var refetchedEntity = entityCo.findById(entity.getId(), entityFetch);
            info.setEntityId(refetchedEntity.getId())
                    .setEntityType(entityCo.getEntityType().getName())
                    .setEntityVersion(refetchedEntity.getVersion())
                    .setCreatedBy(refetchedEntity.getCreatedBy())
                    .setCreatedDate(refetchedEntity.getCreatedDate())
                    .setLastUpdatedBy(refetchedEntity.getLastUpdatedBy())
                    .setLastUpdatedDate(refetchedEntity.getLastUpdatedDate())
                    .setEntityTitle(isEmpty(refetchedEntity.getDesc())
                                            ? refetchedEntity.getKey().toString()
                                            : "%s: %s".formatted(refetchedEntity.getKey(), refetchedEntity.getDesc()));
            return info;
        }
        else {
            throw new InvalidStateException(ERR_NOT_SUITABLE_ENTITY.formatted(entity.getType().getSimpleName()));
        }
    }
}
