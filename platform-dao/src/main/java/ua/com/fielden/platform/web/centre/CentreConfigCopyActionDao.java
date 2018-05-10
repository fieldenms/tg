package ua.com.fielden.platform.web.centre;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;
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
        entity.isValid().ifFailure(Result::throwRuntime);
        
        final String currentSaveAsNameStr = entity.getKey().replaceFirst("default", "");
        System.out.println("currentSaveAsName = " + currentSaveAsNameStr);
        final Optional<String> currentSaveAsName = "".equals(currentSaveAsNameStr) ? empty() : of(currentSaveAsNameStr);
        
        final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntity = criteriaEntityRestorer.restoreCriteriaEntity(entity.getCentreContextHolder());
        final T2<ICentreDomainTreeManagerAndEnhancer, ICentreDomainTreeManagerAndEnhancer> freshAndSaved = criteriaEntity.centreCopier().apply(t2(currentSaveAsName, of(entity.getTitle())), entity.getDesc());
        System.out.println("freshAndSaved = " + freshAndSaved);
        
        return super.save(entity);
    }
    
}
