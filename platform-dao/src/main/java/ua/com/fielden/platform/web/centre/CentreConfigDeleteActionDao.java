package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link CentreConfigDeleteActionCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigDeleteAction.class)
public class CentreConfigDeleteActionDao extends CommonEntityDao<CentreConfigDeleteAction> implements CentreConfigDeleteActionCo {
    
    @Inject
    public CentreConfigDeleteActionDao(final IFilter filter) {
        super(filter);
    }
    
}