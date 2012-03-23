package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao2.IEntityAggregatesDao2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory2;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

import com.google.inject.Inject;

public class EnhancedLocatorEntityQueryCriteria<T extends AbstractEntity, DAO extends IEntityDao2<T>> extends EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @Inject
    public EnhancedLocatorEntityQueryCriteria(final IValueMatcherFactory2 valueMatcherFactory, final IEntityAggregatesDao2 entityAggregatesDao) {
	super(valueMatcherFactory, entityAggregatesDao);
    }
}