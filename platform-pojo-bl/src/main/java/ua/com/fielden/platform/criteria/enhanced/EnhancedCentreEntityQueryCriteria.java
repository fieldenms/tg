package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
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
public class EnhancedCentreEntityQueryCriteria<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ICentreDomainTreeManager, T, DAO> {

    private static final long serialVersionUID = -5189571197523084383L;

    /**
     * Constructs {@link EnhancedCentreEntityQueryCriteria} with specified {@link IEntityAggregatesDao} and {@link IValueMatcherFactory}.
     * Needed mostly for instantiating through injector.
     * 
     * @param entityAggregatesDao
     * @param valueMatcherFactory
     */
    @Inject
    protected EnhancedCentreEntityQueryCriteria(final IEntityAggregatesDao entityAggregatesDao, final IValueMatcherFactory valueMatcherFactory) {
	super(valueMatcherFactory, entityAggregatesDao);
    }


}
