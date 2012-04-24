package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.review.development.EnhancedLocatorEntityQueryCriteria;

import com.google.inject.Inject;

public class LocatorEntityQueryCriteriaToEnhance<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EnhancedLocatorEntityQueryCriteria<T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @Inject
    public LocatorEntityQueryCriteriaToEnhance(final IValueMatcherFactory valueMatcherFactory) {
	super(valueMatcherFactory);
    }
}