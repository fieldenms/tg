package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link CentreConfigDuplicateActionCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigDuplicateAction.class)
public class CentreConfigDuplicateActionDao extends CommonEntityDao<CentreConfigDuplicateAction> implements CentreConfigDuplicateActionCo {
    
    @Inject
    public CentreConfigDuplicateActionDao(final IFilter filter) {
        super(filter);
    }
    
}