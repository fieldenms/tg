package fielden.close_leave;

import com.google.inject.Inject;

import fielden.close_leave.IOpenTgCloseLeaveExampleMasterAction;
import fielden.close_leave.OpenTgCloseLeaveExampleMasterAction;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IOpenTgCloseLeaveExampleMasterAction}.
 * 
 * @author Developers
 *
 */
@EntityType(OpenTgCloseLeaveExampleMasterAction.class)
public class OpenTgCloseLeaveExampleMasterActionDao extends CommonEntityDao<OpenTgCloseLeaveExampleMasterAction> implements IOpenTgCloseLeaveExampleMasterAction {
    
    @Inject
    public OpenTgCloseLeaveExampleMasterActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    protected IFetchProvider<OpenTgCloseLeaveExampleMasterAction> createFetchProvider() {
        // key is needed to be correctly autopopulated by newly saved compound master entity (ID-based restoration of entity-typed key)
        return super.createFetchProvider().with("key");
    }
    
}