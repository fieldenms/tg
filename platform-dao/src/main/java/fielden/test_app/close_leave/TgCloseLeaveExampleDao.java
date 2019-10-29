package fielden.test_app.close_leave;

import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;

import fielden.test_app.close_leave.ITgCloseLeaveExample;
import fielden.test_app.close_leave.ITgCloseLeaveExampleDetail;
import fielden.test_app.close_leave.TgCloseLeaveExample;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetail;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;

/** 
 * DAO implementation for companion object {@link ITgCloseLeaveExample}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCloseLeaveExample.class)
public class TgCloseLeaveExampleDao extends CommonEntityDao<TgCloseLeaveExample> implements ITgCloseLeaveExample {
    
    @Inject
    public TgCloseLeaveExampleDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final List<TgCloseLeaveExample> entities) {
        return defaultBatchDelete(entities);
    }
    
    @Override
    protected IFetchProvider<TgCloseLeaveExample> createFetchProvider() {
        return super.createFetchProvider().with("key", "desc");
    }
    
    @SessionRequired
    @Override
    public TgCloseLeaveExample save(final TgCloseLeaveExample entity) {
        // firstly make sure that the entity is valid
        final Result isValid = entity.isValid();
        if (!isValid.isSuccessful()) {
            throw isValid;
        }
        final boolean wasPersisted = entity.isPersisted();
        final TgCloseLeaveExample savedEntity = super.save(entity);
        
        if (!wasPersisted) {
            // Also create and save an instance of detail entity
            final ITgCloseLeaveExampleDetail coDetail = co$(TgCloseLeaveExampleDetail.class);
            final TgCloseLeaveExampleDetail detailEntity = (TgCloseLeaveExampleDetail) coDetail.new_().setKey(savedEntity).setDesc(savedEntity.getKey() + " detail");
            coDetail.save(detailEntity);
        }
        return savedEntity;
    }
    
}