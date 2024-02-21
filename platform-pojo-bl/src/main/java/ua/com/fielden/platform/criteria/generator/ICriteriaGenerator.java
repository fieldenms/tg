package ua.com.fielden.platform.criteria.generator;

import java.util.Optional;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * A contract for Entity Centre criteria entity generation.
 *
 * @author TG Team
 *
 */
public interface ICriteriaGenerator {
    
    /**
     * Generates Entity Centre criteria entity type with from/to, is/isNot properties; sets the values from {@code centreManager} configuration.
     * <p>
     * Triplet [user; miType; saveAsName] identifies Entity Centre configuration for which criteria entity needs to be generated.
     */
    <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(
        final User user,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final Optional<String> saveAsName,
        final ICentreDomainTreeManagerAndEnhancer centreManager
    );
    
    /**
     * Clears the state in this {@link ICriteriaGenerator} instance.
     * <p>
     * {@link ICriteriaGenerator} is used for generation of selection criteria entity and is thus closely related to Web UI configurations.
     * This method is potentially useful for situations where Web UI configurations should be re-created and invalidated, for e.g. in Eclipse Debug mode
     * to preserve open server when adding / removing selection criteria properties to Web UI centre configurations.
     */
    void clear();
    
}