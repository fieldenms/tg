package ua.com.fielden.platform.serialisation.api;

import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

public interface ICriteriaEntityRestorer {
    
    EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder);
    
}
