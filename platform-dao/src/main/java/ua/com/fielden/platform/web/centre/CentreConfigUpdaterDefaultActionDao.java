package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ICentreConfigUpdaterDefaultAction}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreConfigUpdaterDefaultAction.class)
public class CentreConfigUpdaterDefaultActionDao extends CommonEntityDao<CentreConfigUpdaterDefaultAction> implements ICentreConfigUpdaterDefaultAction {
    
    @Inject
    public CentreConfigUpdaterDefaultActionDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
    }
    
}
