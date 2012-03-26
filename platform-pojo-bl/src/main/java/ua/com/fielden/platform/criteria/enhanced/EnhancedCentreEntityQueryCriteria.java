package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory2;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

import com.google.inject.Inject;

//TODO must finish implementation in order to provide correct ordering, fetch model etc. Consider to provide reference on to the ICriteriaDomainTreeManager.
/**
 * This class is the base class to enhance with criteria and resultant properties.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class EnhancedCentreEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao2<T>> extends EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, DAO> {

    private static final long serialVersionUID = -5189571197523084383L;

    /**
     * Constructs {@link EnhancedCentreEntityQueryCriteria} with specified {@link IValueMatcherFactory}.
     * Needed mostly for instantiating through injector.
     *
     * @param entityDao
     * @param valueMatcherFactory
     */
    @Inject
    protected EnhancedCentreEntityQueryCriteria(final IValueMatcherFactory2 valueMatcherFactory) {
	super(valueMatcherFactory);
    }
}
