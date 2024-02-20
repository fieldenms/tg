package ua.com.fielden.platform.criteria.generator;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * A contract for criteria generator.
 *
 * @author TG Team
 *
 */
public interface ICriteriaGenerator {
    
    /**
     * Generates and configures {@link EntityQueryCriteria} instance.
     *
     * @param <T>
     * @param root
     * @param cdtm
     * @return
     */
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, ICentreDomainTreeManagerAndEnhancer cdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final Annotation... customAnnotations);
    
    /**
     * Generates and configures {@link EntityQueryCriteria} instance.
     *
     * @param <T>
     * @param root
     * @param cdtm
     * @return
     */
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(Class<T> root, ICentreDomainTreeManagerAndEnhancer cdtm, final Annotation... customAnnotations);
    
    /**
     * Clears the state in this {@link ICriteriaGenerator} instance.
     * <p>
     * {@link ICriteriaGenerator} is used for generation of selection criteria entity and is thus closely related to Web UI configurations.
     * This method is potentially useful for situations where Web UI configurations should be re-created and invalidated, for e.g. in Eclipse Debug mode
     * to preserve open server when adding / removing selection criteria properties to Web UI centre configurations.
     */
    void clear();
    
}