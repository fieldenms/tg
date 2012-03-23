package ua.com.fielden.platform.criteria.generator;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
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
    public <T extends AbstractEntity> EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao2<T>> generateCentreQueryCriteria(Class<T> root, ICentreDomainTreeManagerAndEnhancer cdtm);

    public <T extends AbstractEntity> EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao2<T>> generateLocatorQueryCriteria(Class<T> root, ILocatorDomainTreeManagerAndEnhancer ldtm);
}
