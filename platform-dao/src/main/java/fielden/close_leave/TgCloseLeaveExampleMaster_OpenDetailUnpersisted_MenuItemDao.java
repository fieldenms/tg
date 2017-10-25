package fielden.close_leave;

import com.google.inject.Inject;

import fielden.close_leave.ITgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem;
import fielden.close_leave.TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link ITgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem}.
 * 
 * @author Developers
 *
 */
@EntityType(TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem.class)
public class TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItemDao extends CommonEntityDao<TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem> implements ITgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem {
    
    @Inject
    public TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItemDao(final IFilter filter) {
        super(filter);
    }
    
}