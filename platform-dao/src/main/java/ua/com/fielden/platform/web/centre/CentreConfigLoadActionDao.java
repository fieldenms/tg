package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.error.Result.failure;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;

/** 
 * DAO implementation for companion object {@link ICentreConfigLoadAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigLoadAction.class)
public class CentreConfigLoadActionDao extends CommonEntityDao<CentreConfigLoadAction> implements ICentreConfigLoadAction {
    private static final String ERR_EXACTLY_ONE_CONFIGURATION_MUST_BE_SELECTED = "Exactly one configuration must be selected.";
    
    @Inject
    public CentreConfigLoadActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public CentreConfigLoadAction save(final CentreConfigLoadAction entity) {
        entity.isValid().ifFailure(Result::throwRuntime);
        if (entity.getChosenIds().isEmpty()) {
            throw failure(ERR_EXACTLY_ONE_CONFIGURATION_MUST_BE_SELECTED);
        }
        
        return super.save(entity);
    }
    
}