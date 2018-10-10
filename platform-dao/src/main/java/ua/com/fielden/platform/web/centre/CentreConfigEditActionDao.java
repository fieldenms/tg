package ua.com.fielden.platform.web.centre;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigEditAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigEditAction.class)
public class CentreConfigEditActionDao extends AbstractCentreConfigCommitActionDao<CentreConfigEditAction> implements ICentreConfigEditAction {
    
    @Inject
    public CentreConfigEditActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter, criteriaEntityRestorer);
    }
    
    @Override
    protected Map<String, Object> performSave(final CentreConfigEditAction entity) {
        return criteriaEntityRestorer
            .restoreCriteriaEntity(entity.getCentreContextHolder())
            .editCentre(entity.getTitle(), entity.getDesc());
    }
    
}