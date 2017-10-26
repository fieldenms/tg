package ua.com.fielden.platform.web.utils;

import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * An interface for restoring criteria entity from {@link CentreContextHolder} instance.
 * 
 * @author TG Team
 *
 */
public interface ICriteriaEntityRestorer {
    
    /**
     * Restores criteria entity from {@link CentreContextHolder} instance.
     * 
     * @param centreContextHolder
     * @return
     */
    EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder);
    
}
