package ua.com.fielden.platform.criteria.enhanced;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;

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
public class EnhancedEntityQueryCriteria<T extends AbstractEntity, DAO extends IEntityDao<T>> extends EntityQueryCriteria<T, DAO> {

    private static final long serialVersionUID = -5189571197523084383L;

    /**
     * Constructs {@link EnhancedEntityQueryCriteria} with specified {@link IEntityAggregatesDao} and {@link IValueMatcherFactory}.
     * Needed mostly for instantiating through injector.
     * 
     * @param entityAggregatesDao
     * @param valueMatcherFactory
     */
    @Inject
    protected EnhancedEntityQueryCriteria(final IEntityAggregatesDao entityAggregatesDao, final IValueMatcherFactory valueMatcherFactory) {
	super(entityAggregatesDao, valueMatcherFactory);
    }



    @Override
    protected ICompleted createQuery() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected fetch createFetchModel() {
	// TODO Auto-generated method stub
	return null;
    }



}
