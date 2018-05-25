package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * Controller for {@link CentreConfigLoadAction}.
 * 
 * @author TG Team
 *
 */
public class CentreConfigLoadActionController implements ICollectionModificationController<EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>, CentreConfigLoadAction, String, LoadableCentreConfig> {
    
    public CentreConfigLoadActionController() {
    }
    
    @Override
    public EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        return (EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>) context.getSelectionCrit();
    }
    
}
