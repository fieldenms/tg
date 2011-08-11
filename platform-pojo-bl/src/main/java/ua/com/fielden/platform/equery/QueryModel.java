package ua.com.fielden.platform.equery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.equery.tokens.main.JoinConditions;
import ua.com.fielden.platform.equery.tokens.properties.SelectCalculatedProperty;

import static ua.com.fielden.platform.equery.equery.select;
/**
 * Contains finalised query model.
 *
 * @author TG Team
 *
 */
public final class QueryModel<T extends AbstractEntity> implements IQueryModel<T> {
    private final QueryTokens tokens;
    private final Map<String, Object> parameters = new HashMap<String, Object>(); //collects param names and values set by user
    private final Class<T> resultType;
    private boolean lightweight;

    private transient RootEntityMapper masterModelMapper; // main mapper of the master model of this model - e.g. select .. where exists (this query)
    private transient AliasNumerator aliasNumerator;


    public QueryModel(final QueryTokens queryTokens) {
	tokens = queryTokens.clon();
	this.resultType = tokens.getResultType();
    }

    public QueryModel(final QueryTokens queryTokens, final Class<T> resultType) {
	tokens = queryTokens.clon();
	this.resultType = resultType;
    }

    private QueryModel(final QueryTokens queryTokens, final Map<String, Object> parameters, final Class<T> resultType, final boolean lightweight) {
	this.tokens = queryTokens.clon();
	this.parameters.putAll(parameters); // TODO clon?
	this.resultType = resultType;
	this.lightweight = lightweight;
    }

    public QueryModel<T> clon() {
	return new QueryModel<T>(tokens, parameters, resultType, lightweight);
    }

    // This method should be invoked in order for the model to (re-establish) association with its sub-models
    private void setModelHierarchy(final RootEntityMapper masterModelMapper) {
	for (final IQueryModel model : tokens.getSubQueries()) {
	    ((QueryModel) model).masterModelMapper = masterModelMapper;
	    ((QueryModel) model).aliasNumerator = masterModelMapper.getAliasNumerator();
	}
    }

    /**
     * Assign sequential names to internal query parameters.
     *
     * @param alias
     */
    private void assignParamNames(final AliasNumerator aliasNumerator) {
	for (final QueryParameter parameter : tokens.getParameters()) {
	    // assign sequentially generated param name if not assigned
	    if (parameter.getParamName() == null) {
		parameter.setParamName("P" + aliasNumerator.getNextNumber());
	    }
	}
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof QueryModel)) {
	    return false;
	}

	final QueryModel cmp = (QueryModel) obj;
	return tokens.equals(cmp.tokens) && //
		parameters.equals(cmp.parameters);
    }

    private RootEntityMapper produceRootEntityMapper(final IMappingExtractor mappingExtractor, final boolean isReturned, final AliasNumerator aliasNumerator) {
	final List<String> resultantPropertiesAliases = isReturned ? tokens.getResultantPropertiesAliases() : null;
	final RootEntityMapper rootEntityMapper = new RootEntityMapper(tokens.getJoins(), isReturned, mappingExtractor, masterModelMapper, resultantPropertiesAliases, aliasNumerator);
	setModelHierarchy(rootEntityMapper);
	assignParamNames(aliasNumerator);

	return rootEntityMapper;
    }

//    private String getModelSql(final RootEntityMapper rootEntityMapper) {
//	// Important! The order of underlying sql creation is imposed by internal logic and should not be changed
//	try {
//	    final String whereSql = tokens.getWhere().getSql(rootEntityMapper);
//	    //final String groupBySql = tokens.getGroupBy().getSql(rootEntityMapper);
//	    final List<String> groupByProperties = tokens.getGroupBy().getPropertiesAsSql(rootEntityMapper);
//	    //final String orderBySql = tokens.getOrderBy().getSql(rootEntityMapper);
//	    final List<String> orderByProperties = tokens.getOrderBy().getPropertiesAsSql(rootEntityMapper);
//	    final StringBuffer sb = new StringBuffer();
//	    sb.append(whereSql);
//	    sb.append(groupBySql);
//	    sb.append(orderBySql);
//	    return sb.toString();
//	} catch (final Exception e) {
//	    e.printStackTrace();
//	    throw new RuntimeException("Couldn't generate sql for:\n" + tokens + "\ndue to: " + e);
//	}
//
//    }

    @Override
    public ModelResult getModelResult(final IMappingExtractor mappingExtractor) {
	final RootEntityMapper rootEntityMapper = produceRootEntityMapper(mappingExtractor, false, aliasNumerator != null ? aliasNumerator : new AliasNumerator());
	    String whereSql = tokens.getWhere().getSql(rootEntityMapper);
	    whereSql = StringUtils.isNotEmpty(whereSql) ? "\n   WHERE " + whereSql : whereSql;

	    //final String groupBySql = tokens.getGroupBy().getSql(rootEntityMapper);
	    final List<String> groupByProperties = tokens.getGroupBy().getPropertiesAsSql(rootEntityMapper);
	    //final String orderBySql = tokens.getOrderBy().getSql(rootEntityMapper);
	    final List<String> orderByProperties = tokens.getOrderBy().getPropertiesAsSql(rootEntityMapper);

	return tokens.getSelect().getResult(rootEntityMapper, resultType, /*getModelSql(rootEntityMapper)*/ whereSql, groupByProperties, orderByProperties);
    }

    @Override
    public ReturnedModelResult getFinalModelResult(final IMappingExtractor mappingExtractor) {
	final RootEntityMapper rootEntityMapper = produceRootEntityMapper(mappingExtractor, true, aliasNumerator != null ? aliasNumerator : new AliasNumerator());
	String whereSql = tokens.getWhere().getSql(rootEntityMapper);
	    whereSql = StringUtils.isNotEmpty(whereSql) ? "\n   WHERE " + whereSql : whereSql;

	    //final String groupBySql = tokens.getGroupBy().getSql(rootEntityMapper);
	    final List<String> groupByProperties = tokens.getGroupBy().getPropertiesAsSql(rootEntityMapper);
	    //final String orderBySql = tokens.getOrderBy().getSql(rootEntityMapper);
	    final List<String> orderByProperties = tokens.getOrderBy().getPropertiesAsSql(rootEntityMapper);

	return tokens.getSelect().getFinalResult(rootEntityMapper, resultType, /*getModelSql(rootEntityMapper)*/ whereSql, groupByProperties, orderByProperties, getUserParams(), getInternalParams());
    }

    @Override
    public Class<T> getType() {
	return resultType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Parameters related routines ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setParamValue(final String paramName, final Object... values) {
	final QueryParameter parameter = new QueryParameter(paramName, values);
	parameters.put(parameter.getParamName(), parameter.getParamValue());
    }

    public Object getParamValue(final String paramName) {
	if (parameters.containsKey(paramName)) {
	    return parameters.get(paramName);
	} else {
	    throw new RuntimeException("Couldn't find parameter [" + paramName + "]");
	}
    }

    private List<QueryParameter> getInternalParams() {
	final List<QueryParameter> result = new ArrayList<QueryParameter>();

	// populate to result all params names/values that exist within query and all its subqueries
	result.addAll(getInternalParams(tokens));

	return Collections.unmodifiableList(result);
    }

    private List<QueryParameter> getInternalParams(final QueryTokens queryTokens) {
	final List<QueryParameter> result = new ArrayList<QueryParameter>();

	// populate to result all params names/values that exist within query
	for (final QueryParameter parameter : queryTokens.getParameters()) {
	    result.add(parameter);
	}

	for (final IQueryModel subQuery : queryTokens.getSubQueries()) {
	    result.addAll(((QueryModel) subQuery).getInternalParams());
	}

	for (final JoinConditions joinConditions : queryTokens.getJoins()) {
	    for (final IQueryModel sourceModel : joinConditions.getQuerySource().getModels()) {
		result.addAll(((QueryModel) sourceModel).getInternalParams());
	    }
	}

	return Collections.unmodifiableList(result);
    }

    private Map<String, Object> getUserParams() {
	final Map<String, Object> result = new HashMap<String, Object>();

	// populate to result all params names/values that exist within query and all its subqueries
	result.putAll(parameters);

	result.putAll(getUserParamValues(tokens));

	return Collections.unmodifiableMap(result);
    }

    private Map<String, Object> getUserParamValues(final QueryTokens queryTokens) {
	final Map<String, Object> result = new HashMap<String, Object>();

	for (final IQueryModel subQuery : queryTokens.getSubQueries()) {
	    result.putAll(((QueryModel) subQuery).getUserParams());
	}

	for (final JoinConditions joinConditions : queryTokens.getJoins()) {
	    for (final IQueryModel sourceModel : joinConditions.getQuerySource().getModels()) {
		result.putAll(((QueryModel) sourceModel).getUserParams());
	    }
	}

	return Collections.unmodifiableMap(result);
    }

    @Override
    public List<String> getYieldedPropsNames() {
	final List<String> result = new ArrayList<String>();
	for (final SelectCalculatedProperty prop : tokens.getSelect().getSelectCalculatedProps()) {
	    result.add(prop.getPropertyAlias());
	}
	return result;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Derived models related routines ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public IQueryModel<EntityAggregates> getCountModel() {
	final IQueryModel<EntityAggregates> countModelBase = new QueryModel<EntityAggregates>(tokens.clonAsUnorderedModel());
	final IQueryModel<EntityAggregates> countModel = select(countModelBase).yieldExp("COUNT(*)", "RECORDS_COUNT").model(EntityAggregates.class);

	((QueryModel) countModel).parameters.putAll(parameters); // copying parameters set by user
	return countModel;
    }

//    @Override
//    public Pair<IQueryModel<T>, List<Pair<String, Ordering>>> decomposeToModelAndOrdering() {
//	final IQueryModel<T> unorderedModel = new QueryModel<T>(tokens.clonAsUnorderedModel());
//	((QueryModel) unorderedModel).copyParameterValuesFrom(this);
//
//	final List<Pair<String, Ordering>> orderings = new ArrayList<Pair<String, Ordering>>();
//	for (final OrderByProperty orderByProperty : tokens.getOrderBy().getProperties()) {
//	    orderings.add(new Pair<String, Ordering>(orderByProperty.getRawValue(), orderByProperty.getOrdering()));
//	};
//
//	return new Pair<IQueryModel<T>, List<Pair<String, Ordering>>>(unorderedModel, orderings);
//    }

    @Override
    public IQueryOrderedModel<AbstractEntity> getModelWithAbstractEntities() {
	return new QueryModel<AbstractEntity>(this.tokens);
    }

    public AliasNumerator getAliasNumerator() {
	return aliasNumerator;
    }

    public void setAliasNumerator(final AliasNumerator aliasNumerator) {
	this.aliasNumerator = aliasNumerator;
    }

    @Override
    public boolean isLightweight() {
	return lightweight;
    }

    @Override
    public void setLightweight(final boolean lightweight) {
	this.lightweight = lightweight;
    }

    @Override
    public IQueryOrderedModel<T> enhanceWith(final IFilter filter, final String userName) {
	final IQueryOrderedModel<T> result = clon();
	if (filter != null) {
	    ((QueryModel)result).tokens.getJoins().get(0).getQuerySource().applyFilter(filter, userName);
	    return result;
	}

	return this;
    }
}
