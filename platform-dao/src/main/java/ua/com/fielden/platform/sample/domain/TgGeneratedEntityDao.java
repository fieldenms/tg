package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
/** 
 * DAO implementation for companion object {@link ITgGeneratedEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(TgGeneratedEntity.class)
public class TgGeneratedEntityDao extends CommonEntityDao<TgGeneratedEntity> implements ITgGeneratedEntity {

    @Inject
    public TgGeneratedEntityDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<TgGeneratedEntity> model) {
        return defaultBatchDelete(model);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final List<TgGeneratedEntity> entities) {
        return defaultBatchDelete(entities);
    }
    
    @Override
    protected IFetchProvider<TgGeneratedEntity> createFetchProvider() {
        return super.createFetchProvider().with("key", "createdBy");
    }
    
}