package ua.com.fielden.platform.criteria.generator;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A contract for Entity Centre criteria entity generation.
 *
 * @author TG Team
 *
 */
public interface ICriteriaGenerator {
    
    /**
     * Generates Entity Centre criteria entity type with from/to, is/isNot properties; sets the values from {@code centreManager} configuration.
     */
    <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final ICentreDomainTreeManagerAndEnhancer centreManager);

}