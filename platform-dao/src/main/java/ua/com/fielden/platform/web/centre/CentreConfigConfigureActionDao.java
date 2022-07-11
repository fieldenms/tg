package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link CentreConfigConfigureActionCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigConfigureAction.class)
public class CentreConfigConfigureActionDao extends CommonEntityDao<CentreConfigConfigureAction> implements CentreConfigConfigureActionCo {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigConfigureActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    @SessionRequired
    public CentreConfigConfigureAction save(final CentreConfigConfigureAction action) {
        // validate action entity before performing actual action
        action.isValid().ifFailure(Result::throwRuntime);
        
        // perform actual action
        action.setCustomObject(criteriaEntityRestorer
            .restoreCriteriaEntity(action.getCentreContextHolder())
            .configureCentre(action.isRunAutomatically())
        );
        return action;
    }
    
}