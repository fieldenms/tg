package fielden.close_leave;

import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;

import fielden.close_leave.ITgCloseLeaveExampleDetail;
import fielden.close_leave.TgCloseLeaveExampleDetail;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ITgCloseLeaveExampleDetail}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCloseLeaveExampleDetail.class)
public class TgCloseLeaveExampleDetailDao extends CommonEntityDao<TgCloseLeaveExampleDetail> implements ITgCloseLeaveExampleDetail {
    
    @Inject
    public TgCloseLeaveExampleDetailDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final List<TgCloseLeaveExampleDetail> entities) {
        return defaultBatchDelete(entities);
    }
    
    @Override
    protected IFetchProvider<TgCloseLeaveExampleDetail> createFetchProvider() {
        return super.createFetchProvider().with("key" /* key is needed here because getDesc() method of AE is dependent on 'key'... */, "desc");
    }
    
}