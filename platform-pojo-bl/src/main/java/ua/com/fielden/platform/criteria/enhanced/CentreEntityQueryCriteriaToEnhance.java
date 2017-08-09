package ua.com.fielden.platform.criteria.enhanced;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

//TODO must finish implementation in order to provide correct ordering, fetch model etc. Consider to provide reference on to the ICriteriaDomainTreeManager.
/**
 * This class is the base class to enhance with criteria and resultant properties.
 * 
 * @author TG Team
 * 
 * @param <T>
 * @param <DAO>
 */
@EntityTitle("Centre Selection Criteria")
public class CentreEntityQueryCriteriaToEnhance<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EnhancedCentreEntityQueryCriteria<T, DAO> {

    private static final long serialVersionUID = -5189571197523084383L;

    /**
     * Constructs {@link CentreEntityQueryCriteriaToEnhance} with specified {@link IValueMatcherFactory}. Needed mostly for instantiating through injector.
     * 
     * @param entityDao
     * @param valueMatcherFactory
     */
    @SuppressWarnings("rawtypes")
    @Inject
    protected CentreEntityQueryCriteriaToEnhance(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider) {
        super(valueMatcherFactory, generatedEntityController, serialiser, controllerProvider);
    }
}
