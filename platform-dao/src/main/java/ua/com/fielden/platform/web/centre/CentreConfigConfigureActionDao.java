package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigConfigureAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigConfigureAction.class)
public class CentreConfigConfigureActionDao extends CommonEntityDao<CentreConfigConfigureAction> implements ICentreConfigConfigureAction {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigConfigureActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    @SessionRequired
    public CentreConfigConfigureAction save(final CentreConfigConfigureAction entity) {
        // validate action entity before performing actual action
        entity.isValid().ifFailure(Result::throwRuntime);
        
        // perform actual action
        entity.setCustomObject(criteriaEntityRestorer
            .restoreCriteriaEntity(entity.getCentreContextHolder())
            .configureCentre(entity.isRunAutomatically())
        );
        return entity;
    }
    
}