package ua.com.fielden.platform.web.centre;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigSaveAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigSaveAction.class)
public class CentreConfigSaveActionDao extends AbstractCentreConfigCommitActionDao<CentreConfigSaveAction> implements ICentreConfigSaveAction {
    
    @Inject
    public CentreConfigSaveActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter, criteriaEntityRestorer);
    }
    
    @Override
    protected Map<String, Object> performSave(final CentreConfigSaveAction entity) {
        return criteriaEntityRestorer
            .restoreCriteriaEntity(entity.getCentreContextHolder())
            .saveCentre(entity.getTitle(), entity.getDesc());
    }
    
}