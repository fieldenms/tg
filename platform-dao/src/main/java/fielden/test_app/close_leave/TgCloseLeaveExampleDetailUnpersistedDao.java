package fielden.test_app.close_leave;

import com.google.inject.Inject;

import fielden.test_app.close_leave.ITgCloseLeaveExampleDetailUnpersisted;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetailUnpersisted;

import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.dao.annotations.SessionRequired;

/** 
 * DAO implementation for companion object {@link ITgCloseLeaveExampleDetailUnpersisted}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCloseLeaveExampleDetailUnpersisted.class)
public class TgCloseLeaveExampleDetailUnpersistedDao extends CommonEntityDao<TgCloseLeaveExampleDetailUnpersisted> implements ITgCloseLeaveExampleDetailUnpersisted {
    
    @Inject
    public TgCloseLeaveExampleDetailUnpersistedDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final List<TgCloseLeaveExampleDetailUnpersisted> entities) {
        return defaultBatchDelete(entities);
    }
    
    @Override
    protected IFetchProvider<TgCloseLeaveExampleDetailUnpersisted> createFetchProvider() {
        return super.createFetchProvider().with("key" /* key is needed here because getDesc() method of AE is dependent on 'key'... */, "desc");
    }
    
}