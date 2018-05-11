package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;

/** 
 * DAO implementation for companion object {@link ICentreConfigDeleteAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigDeleteAction.class)
public class CentreConfigDeleteActionDao extends CommonEntityDao<CentreConfigDeleteAction> implements ICentreConfigDeleteAction {
    
    @Inject
    public CentreConfigDeleteActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public CentreConfigDeleteAction save(final CentreConfigDeleteAction entity) {
        entity.isValid().ifFailure(Result::throwRuntime); // TODO
        return super.save(entity);
    }
    
}
