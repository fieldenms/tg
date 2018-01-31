package fielden.test_app.close_leave;

import com.google.inject.Inject;

import fielden.test_app.close_leave.ITgCloseLeaveExampleMaster_OpenDetail_MenuItem;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenDetail_MenuItem;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link ITgCloseLeaveExampleMaster_OpenDetail_MenuItem}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCloseLeaveExampleMaster_OpenDetail_MenuItem.class)
public class TgCloseLeaveExampleMaster_OpenDetail_MenuItemDao extends CommonEntityDao<TgCloseLeaveExampleMaster_OpenDetail_MenuItem> implements ITgCloseLeaveExampleMaster_OpenDetail_MenuItem {
    
    @Inject
    public TgCloseLeaveExampleMaster_OpenDetail_MenuItemDao(final IFilter filter) {
        super(filter);
    }
    
}