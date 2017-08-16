package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * Controller for {@link CentreConfigUpdater}.
 * 
 * @author TG Team
 *
 */
public class CentreConfigUpdaterController implements ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater, String, CustomisableColumn> {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    public CentreConfigUpdaterController(final ICriteriaEntityRestorer criteriaEntityRestorer) {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        return (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) context.getSelectionCrit();
    }
    
    /**
     * Overridden to restore master entity (criteria entity) from masterEntityHolder that was initialised in producer.
     */
    @Override
    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> getMasterEntityFromAction(final CentreConfigUpdater action) {
        return (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) criteriaEntityRestorer.restoreCriteriaEntity(action.getMasterEntityHolder());
    }
    
}
