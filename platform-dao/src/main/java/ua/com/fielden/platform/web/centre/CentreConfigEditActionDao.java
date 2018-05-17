package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.centre.CentreConfigEditAction.EditKind.valueOf;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigEditAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigEditAction.class)
public class CentreConfigEditActionDao extends CommonEntityDao<CentreConfigEditAction> implements ICentreConfigEditAction {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigEditActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    @SessionRequired
    public CentreConfigEditAction save(final CentreConfigEditAction entity) {
        // validate centre configuration edit / copy action before performing actual edit / copy
        entity.isValid().ifFailure(Result::throwRuntime);
        
        // perform actual copy using centreCopier() closure
        criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder())
            .centreEditor().accept(t2(valueOf(entity.getEditKind()), of(entity.getTitle())), entity.getDesc());
        return super.save(entity);
    }
    
}