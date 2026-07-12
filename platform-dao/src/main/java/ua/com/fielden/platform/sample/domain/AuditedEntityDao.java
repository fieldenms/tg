package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@EntityType(AuditedEntity.class)
public class AuditedEntityDao extends CommonEntityDao<AuditedEntity> implements AuditedEntityCo {

    @Override
    public void delete(final AuditedEntity entity) {
        defaultDelete(entity);
    }

    @Override
    public void delete(final EntityResultQueryModel<AuditedEntity> query) {
        defaultDelete(query);
    }

    @Override
    public void delete(final EntityResultQueryModel<AuditedEntity> query, final Map<String, Object> paramValues) {
        defaultDelete(query, paramValues);
    }

    @Override
    public int batchDelete(final List<AuditedEntity> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    public int batchDelete(final EntityResultQueryModel<AuditedEntity> query) {
        return defaultBatchDelete(query);
    }

    @Override
    public int batchDelete(final EntityResultQueryModel<AuditedEntity> model, final Map<String, Object> paramValues) {
        return defaultBatchDelete(model, paramValues);
    }

    @Override
    public <E extends AbstractEntity<?>> int batchDeleteByPropertyValues(
            final String propName,
            final List<E> propEntities)
    {
        return defaultBatchDeleteByPropertyValues(propName, propEntities);
    }

    @Override
    public int batchDeleteByPropertyValues(final String propName, final Collection<Long> propEntitiesIds) {
        return defaultBatchDeleteByPropertyValues(propName, propEntitiesIds);
    }

}
