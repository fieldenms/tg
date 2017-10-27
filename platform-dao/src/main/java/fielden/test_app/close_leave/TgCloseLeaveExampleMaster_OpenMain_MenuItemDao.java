package fielden.test_app.close_leave;

import com.google.inject.Inject;

import fielden.test_app.close_leave.ITgCloseLeaveExampleMaster_OpenMain_MenuItem;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenMain_MenuItem;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link ITgCloseLeaveExampleMaster_OpenMain_MenuItem}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCloseLeaveExampleMaster_OpenMain_MenuItem.class)
public class TgCloseLeaveExampleMaster_OpenMain_MenuItemDao extends CommonEntityDao<TgCloseLeaveExampleMaster_OpenMain_MenuItem> implements ITgCloseLeaveExampleMaster_OpenMain_MenuItem {
    
    @Inject
    public TgCloseLeaveExampleMaster_OpenMain_MenuItemDao(final IFilter filter) {
        super(filter);
    }
    
}