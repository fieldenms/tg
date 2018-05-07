package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ICentreConfigCopyAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigCopyAction.class)
public class CentreConfigCopyActionDao extends CommonEntityDao<CentreConfigCopyAction> implements ICentreConfigCopyAction {
    
    @Inject
    public CentreConfigCopyActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public CentreConfigCopyAction save(final CentreConfigCopyAction action) {
        return super.save(action);
    }
    
}
