package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ICentreConfigNewAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigNewAction.class)
public class CentreConfigNewActionDao extends CommonEntityDao<CentreConfigNewAction> implements ICentreConfigNewAction {
    
    @Inject
    public CentreConfigNewActionDao(final IFilter filter) {
        super(filter);
    }
    
}