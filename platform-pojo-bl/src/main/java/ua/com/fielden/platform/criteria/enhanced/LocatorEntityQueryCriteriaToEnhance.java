package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.review.development.EnhancedLocatorEntityQueryCriteria;

import com.google.inject.Inject;

public class LocatorEntityQueryCriteriaToEnhance<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EnhancedLocatorEntityQueryCriteria<T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @SuppressWarnings("rawtypes")
    @Inject
    public LocatorEntityQueryCriteriaToEnhance(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider) {
	super(valueMatcherFactory, generatedEntityController, serialiser, controllerProvider);
    }
}