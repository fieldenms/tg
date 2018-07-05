package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;

import java.util.Map;
import java.util.Optional;

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
public class CentreConfigSaveActionDao extends AbstractCentreConfigEditActionDao<CentreConfigSaveAction> implements ICentreConfigSaveAction {
    
    @Inject
    public CentreConfigSaveActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter, criteriaEntityRestorer);
    }
    
    @Override
    protected Map<String, Object> performSave(final CentreConfigSaveAction entity) {
        return criteriaEntityRestorer
            .restoreCriteriaEntity(entity.getCentreContextHolder())
            .centreSaver()
            .apply(
                of(entity.getTitle()),
                entity.getDesc()
            );
    }
    
}