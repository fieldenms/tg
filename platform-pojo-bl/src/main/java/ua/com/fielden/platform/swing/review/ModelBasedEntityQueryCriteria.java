package ua.com.fielden.platform.swing.review;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.equery.IPropertyAggregationFunction;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.treemodel.IPropertyFilter;

public class ModelBasedEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends DynamicEntityQueryCriteria<T, DAO> {

    private static final long serialVersionUID = -5153076412786128830L;

    private final ICompleted queryModel;
    private final AggregatedResultQueryModel queryTotalsModel;
    private final Map<String, String> totalsDescs = new HashMap<String, String>();

    private IPropertyFilter propertyFilter = new DefaultModelBasedCriteriaPropertyFilter();

    @Override
    public IPropertyFilter getCriteriaFilter() {
	return propertyFilter;
    }

    public ModelBasedEntityQueryCriteria(final EntityFactory entityFactory, final DAO dao, final IEntityAggregatesDao entityAggregatesDao, final ICompleted queryModel, final AggregatedResultQueryModel queryTotalsModel, final Map<String, String> totalsDescs) {
	super(entityFactory, null, dao, entityAggregatesDao, null, null);
	this.queryModel = queryModel;
	this.queryTotalsModel = queryTotalsModel;
	this.totalsDescs.putAll(totalsDescs);
	for (final Field propField : Finder.findProperties(dao.getEntityType())) {
	    final DynamicCriteriaPropertyAnalyser propertyAnalyser = new DynamicCriteriaPropertyAnalyser(getEntityClass(), propField.getName(), propertyFilter);
	    if (propertyAnalyser.isPropertyVisible() && propertyAnalyser.isFetchPropertyAvailable()) {
		addFetchProperty(propField.getName());
		if (true) {// queryTotalsModel.getYieldedPropsNames().contains(propField.getName() + "_total")) {
		    addTotal(propField.getName(), new IPropertyAggregationFunction() {

			@Override
			public Class<?> getReturnedType(final Class<?> propertyClass) {
			    return propField.getType();
			}

			@Override
			public String createQueryString(final String key) {
			    return null;
			}

			@Override
			public String toString() {
			    return totalsDescs.get(propField.getName());
			}
		    });
		}
	    }
	}
    }

    @Override
    public ICompleted createQuery() {
	return queryModel;
    }

    @Override
    public String getAlias() {
	return null;
    }

    @Override
    protected AggregatedResultQueryModel createQueryWithTotals() {
	return queryTotalsModel;
    }

    @Override
    public String getAliasForTotalsProperty(final String propertyName) {
	final String totalPropName = propertyName + "_total";
	if (true) {//queryTotalsModel.getYieldedPropsNames().contains(totalPropName)) {
	    return totalPropName;
	} else {
	    return null;
	}

    }

    @Override
    public boolean isTotalsPresent(final String propertyName) {
	return getAliasForTotalsProperty(propertyName) != null;
    }
}
