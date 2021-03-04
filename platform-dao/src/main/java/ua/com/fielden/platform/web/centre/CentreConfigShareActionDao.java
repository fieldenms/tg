package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ICentreConfigShareAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigShareAction.class)
public class CentreConfigShareActionDao extends CommonEntityDao<CentreConfigShareAction> implements ICentreConfigShareAction {
    
    @Inject
    public CentreConfigShareActionDao(final IFilter filter) {
        super(filter);
    }
    
}