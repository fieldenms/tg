package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

public class CentreConfigUpdaterController implements ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigUpdater, String, CustomisableColumn> {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;
    
    public CentreConfigUpdaterController(final ICriteriaEntityRestorer criteriaEntityRestorer) {
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }
    
    @Override
    public AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        return context.getSelectionCrit();
    }
    
    @Override
    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> refetchMasterEntity(final AbstractEntity<?> masterEntityFromContext) {
        return (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) masterEntityFromContext;
    }
    
    /**
     * Retrieves master entity from action entity.
     * 
     * TODO some caching?
     * TODO return type MASTER_TYPE?
     * 
     * @return
     */
    @Override
    public AbstractEntity<?> getMasterEntityFromAction(final CentreConfigUpdater action) {
        return criteriaEntityRestorer.restoreCriteriaEntity(action.getMasterEntityHolder());
    }

}
