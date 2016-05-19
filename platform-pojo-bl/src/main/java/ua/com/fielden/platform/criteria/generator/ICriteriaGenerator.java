package ua.com.fielden.platform.criteria.generator;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EnhancedLocatorEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

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
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, ICentreDomainTreeManagerAndEnhancer cdtm, final Class<?> miType, final Annotation... customAnnotations);

    /**
     * Generates and configures {@link EntityQueryCriteria} instance.
     *
     * @param <T>
     * @param root
     * @param cdtm
     * @return
     */
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(Class<T> root, ICentreDomainTreeManagerAndEnhancer cdtm, final Annotation... customAnnotations);

    public <T extends AbstractEntity<?>> EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>> generateLocatorQueryCriteria(Class<T> root, ILocatorDomainTreeManagerAndEnhancer ldtm, final Annotation... customAnnotations);
}
