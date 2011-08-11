package ua.com.fielden.platform.swing.review;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoin;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.reportquery.IDistributedProperty;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * {@link DynamicEntityQueryCriteria} that for details view.
 *
 * @author oleh
 *
 * @param <T>
 * @param <DAO>
 */
public class DynamicEntityQueryCriteriaWithParameter<T extends AbstractEntity, DAO extends IEntityDao<T>> extends DynamicEntityQueryCriteria<T, DAO> {

    private static final long serialVersionUID = 5320839052470385053L;

    private final List<Pair<IDistributedProperty, Object>> parameters = new ArrayList<Pair<IDistributedProperty, Object>>();

    public DynamicEntityQueryCriteriaWithParameter(final DynamicEntityQueryCriteria<T, DAO> criteria, final List<Pair<IDistributedProperty, Object>> parameters) {
	super(criteria.getCriteriaEntityFactory(), criteria.getDaoFactory(), (DAO) criteria.getDaoFactory().newDao(criteria.getEntityClass()), criteria.getEntityAggregatesDao(), criteria.getValueMatcherFactory(), null);
	criteria.copyTo(this);
	this.parameters.clear();
	if (parameters != null) {
	    this.parameters.addAll(parameters);
	}
	for (final Pair<IDistributedProperty, Object> pair : this.parameters) {
	    pair.getKey().setTableAlias(criteria.getAlias());
	}
    }

    @Override
    public boolean isDefaultEnabled() {
	return false;
    }

    @Override
    protected ICompleted createQuery() {
	ICompleted completed = DynamicQueryBuilder.buildConditions(createJoinCondition(), createQueryProperties(), getAlias());
	for (final Pair<IDistributedProperty, Object> entry : parameters) {
	    final IWhere where = createWhereCondition(completed);
	    final ISearchCondition searchCondition = entry.getKey().isExpression() ? where.exp(entry.getKey().getParsedValue()) :
	    /*								*/where.prop(entry.getKey().getParsedValue());
	    if (entry.getValue() == null) {
		completed = searchCondition.isNull();
	    } else if (isBoolean(entry.getValue().getClass())) {
		completed = ((Boolean) entry.getValue()) ? searchCondition.isTrue() : searchCondition.isFalse();
	    } else {
		completed = searchCondition.eq().val(entry.getValue());
	    }
	    //	    if (entry.getKey().isExpression()) {
	    //		completed = entry.getValue() == null ? where.exp(entry.getKey().getParsedValue()).isNull() : //
	    //			where.exp(entry.getKey().getParsedValue()).eq().val(entry.getValue());
	    //	    } else {
	    //		completed = entry.getValue() == null ? where.prop(entry.getKey().getParsedValue()).isNull() : //
	    //			where.prop(entry.getKey().getParsedValue()).eq().val(entry.getValue());
	    //	    }
	}
	completed = completed.resultType(getEntityClass());
	return completed;
    }

    /**
     * 'Overridden' because non-simple 'boolean' type should be used.
     *
     * @param type
     * @return
     */
    private boolean isBoolean(final Class<?> type) {
	return EntityUtils.isBoolean(type) || Boolean.class.isAssignableFrom(type);
    }

    private IWhere createWhereCondition(final ICompleted completed) {
	return (completed instanceof IJoin) ? ((IJoin) completed).where() : ((ICompoundCondition) completed).and();
    }
}
