package ua.com.fielden.platform.sample.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/** 
 * DAO implementation for companion object {@link ITgSystem}.
 * 
 * @author Developers
 *
 */
@EntityType(TgSystem.class)
public class TgSystemDao extends CommonEntityDao<TgSystem> implements ITgSystem {
    
    @Inject
    public TgSystemDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public void delete(TgSystem entity) {
        defaultDelete(entity);
    }

    @Override
    public void delete(final EntityResultQueryModel<TgSystem> model) {
        defaultDelete(model);
    }

    @Override
    public void delete(final EntityResultQueryModel<TgSystem> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

    @Override
    public int batchDelete(final EntityResultQueryModel<TgSystem> model) {
        return defaultBatchDelete(model);
    }

    @Override
    public int batchDelete(final EntityResultQueryModel<TgSystem> model, final Map<String, Object> paramValues) {
        return defaultBatchDelete(model, paramValues);
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    public int batchDelete(final List<TgSystem> entities){
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