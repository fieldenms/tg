package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.development.IValueMatcherFactory;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

import com.google.inject.Inject;

public class EnhancedLocatorEntityQueryCriteria<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ILocatorDomainTreeManager, T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @Inject
    public EnhancedLocatorEntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IEntityAggregatesDao entityAggregatesDao) {
	super(valueMatcherFactory, entityAggregatesDao);
	// TODO Auto-generated constructor stub
    }

}
