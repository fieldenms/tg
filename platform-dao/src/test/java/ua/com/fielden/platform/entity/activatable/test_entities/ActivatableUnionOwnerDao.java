package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@EntityType(ActivatableUnionOwner.class)
public class ActivatableUnionOwnerDao extends CommonEntityDao<ActivatableUnionOwner> implements ActivatableUnionOwnerCo {

    @Override
    public void delete(final ActivatableUnionOwner entity) {
        defaultDelete(entity);
    }

    @Override
    public void delete(final EntityResultQueryModel<ActivatableUnionOwner> model) {
        defaultDelete(model);
    }

    @Override
    public void delete(final EntityResultQueryModel<ActivatableUnionOwner> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

    @Override
    public int batchDelete(final EntityResultQueryModel<ActivatableUnionOwner> model) {
        return defaultBatchDelete(model);
    }

    @Override
    public int batchDelete(final EntityResultQueryModel<ActivatableUnionOwner> model, final Map<String, Object> paramValues) {
        return defaultBatchDelete(model, paramValues);
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    public int batchDelete(final List<ActivatableUnionOwner> entities){
        return defaultBatchDelete(entities);
    }

    @Override
    public int batchDeleteByPropertyValues(final String propName, final Collection<Long> propEntitiesIds) {
        return defaultBatchDeleteByPropertyValues(propName, propEntitiesIds);
    }

    @Override
    public <E extends AbstractEntity<?>> int batchDeleteByPropertyValues(final String propName, final List<E> propEntities) {
        return defaultBatchDeleteByPropertyValues(propName, propEntities);
    }

}
