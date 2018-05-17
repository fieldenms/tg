package ua.com.fielden.platform.web.centre;

import static java.util.Optional.of;
import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/** 
 * DAO implementation for companion object {@link ICentreConfigCopyAction}.
 * 
 * @author TG Team
 *
 */
@EntityType(CentreConfigCopyAction.class)
public class CentreConfigCopyActionDao extends CommonEntityDao<CentreConfigCopyAction> implements ICentreConfigCopyAction {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    @Inject
    public CentreConfigCopyActionDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    @SessionRequired
    public CentreConfigCopyAction save(final CentreConfigCopyAction entity) {
        // validate centre configuration copy action before performing actual copy
        entity.isValid().ifFailure(Result::throwRuntime);
        
        // perform actual copy using centreCopier() closure
        criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder())
            .centreCopier().accept(of(entity.getTitle()), entity.getDesc());
        return super.save(entity);
    }
    
}