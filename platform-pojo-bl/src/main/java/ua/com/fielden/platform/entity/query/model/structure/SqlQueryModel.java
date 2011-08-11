package ua.com.fielden.platform.entity.query.model.structure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class SqlQueryModel {
    private final List<Pair<TokenCategory, Object>> tokens;
    private final Map<String, Object> parametersValues = new HashMap<String, Object>(); //collects values for named params set by user
    Class resultType;
    private boolean lightweight;

    private transient SqlQueryModel masterModel;

    @Override
    public SqlQueryModel clone() {
	try {
	    return (SqlQueryModel) super.clone();
	} catch (final CloneNotSupportedException e) {
	    throw new AssertionError(); // Can't happen
	}
    }

    public SqlQueryModel copyWithMasterModel(final SqlQueryModel masterModel) {
	final SqlQueryModel copy = this.clone();
	copy.setMasterModel(masterModel);
	return copy;
    }

    protected int getMasterModelLevel() {
	int result = 0;
	SqlQueryModel currMasterModel = this.masterModel;
	while (currMasterModel != null) {
	    result = result + 1;
	    currMasterModel = currMasterModel.getMasterModel();
	}

	return result;
    }

//    public QueryModelResult getQueryModelResult() {
//	final String sql = tokens.getSql(getMasterModelLevel());
//	final QueryModelResult result = new QueryModelResult(resultType, sql);
//
////	int paramIndex = 0;
////	for (final QueryParameter qryParam : getAllParams()) {
////	    if (qryParam.getParamName() == null) {
////		paramIndex = paramIndex + 1;
////		result.getParamValues().put("P_" + paramIndex, qryParam.getParamValue());
////	    } else {
////		result.getParamValues().put(qryParam.getParamName(), parametersValues.get(qryParam.getParamName()));
////	    }
////	}
//	return result;
//    }
//
//    public SubQueryModelResult getSubQueryModelResult() {
//	final String sql = tokens.getSql(getMasterModelLevel());
//	return new SubQueryModelResult(resultType, sql);
//    }


    @Override
    public String toString() {
        return tokens.toString();
    }

    public SqlQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	this.tokens = tokens;
    }

    public SqlQueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class resultType) {
	this(tokens);
	this.resultType = resultType;
    }

//    // This method should be invoked in order for the model to (re-establish) association with its sub-models
//    private void setModelHierarchy(final RootEntityMapper masterModelMapper) {
//	for (final IQueryModel model : tokens.getSubQueries()) {
//	    ((QueryModel) model).masterModelMapper = masterModelMapper;
//	    ((QueryModel) model).aliasNumerator = masterModelMapper.getAliasNumerator();
//	}
//    }
//
//    /**
//     * Assign sequential names to internal query parameters.
//     *
//     * @param alias
//     */
//    private void assignParamNames(final AliasNumerator aliasNumerator) {
//	for (final QueryParameter parameter : tokens.getParameters()) {
//	    // assign sequentially generated param name if not assigned
//	    if (parameter.getParamName() == null) {
//		parameter.setParamName("P" + aliasNumerator.getNextNumber());
//	    }
//	}
//    }
//
//    private RootEntityMapper produceRootEntityMapper(final IMappingExtractor mappingExtractor, final boolean isReturned, final AliasNumerator aliasNumerator) {
//	final List<String> resultantPropertiesAliases = isReturned ? tokens.getResultantPropertiesAliases() : null;
//	final RootEntityMapper rootEntityMapper = new RootEntityMapper(tokens.getJoins(), isReturned, mappingExtractor, masterModelMapper, resultantPropertiesAliases, aliasNumerator);
//	setModelHierarchy(rootEntityMapper);
//	assignParamNames(aliasNumerator);
//
//	return rootEntityMapper;
//    }
//
//    private String getModelSql(final RootEntityMapper rootEntityMapper) {
//	// Important! The order of underlying sql creation is imposed by internal logic and should not be changed
//	try {
//	    String whereSql = tokens.getWhere().getSql(rootEntityMapper);
//	    whereSql = StringUtils.isNotEmpty(whereSql) ? "\n   WHERE " + whereSql : whereSql;
//	    final String groupBySql = tokens.getGroupBy().getSql(rootEntityMapper);
//	    final String orderBySql = tokens.getOrderBy().getSql(rootEntityMapper);
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
//
//    @Override
//    public ModelResult getModelResult(final IMappingExtractor mappingExtractor) {
//	final RootEntityMapper rootEntityMapper = produceRootEntityMapper(mappingExtractor, false, aliasNumerator != null ? aliasNumerator : new AliasNumerator());
//	return tokens.getSelect().getResult(rootEntityMapper, resultType, getModelSql(rootEntityMapper));
//    }
//
//    @Override
//    public ReturnedModelResult getFinalModelResult(final IMappingExtractor mappingExtractor) {
//	final RootEntityMapper rootEntityMapper = produceRootEntityMapper(mappingExtractor, true, aliasNumerator != null ? aliasNumerator : new AliasNumerator());
//	return tokens.getSelect().getFinalResult(rootEntityMapper, resultType, getModelSql(rootEntityMapper), getUserParams(), getInternalParams());
//    }

    public Class<?> getType() {
	return resultType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Parameters related routines ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setParamValue(final String paramName, final Object... values) {
	final QueryParameter parameter = new QueryParameter(paramName, values);
	parametersValues.put(parameter.getParamName(), parameter.getParamValue());
    }

    public Object getParamValue(final String paramName) {
	if (parametersValues.containsKey(paramName)) {
	    return parametersValues.get(paramName);
	} else {
	    throw new RuntimeException("Couldn't find parameter [" + paramName + "]");
	}
    }

//    /**
//     * Collects all parameters from this query and all its source- and sub- queries taking into account their nesting nature.
//     * @return
//     */
//    private List<QueryParameter> getAllParams() {
//	final List<QueryParameter> result = new ArrayList<QueryParameter>();
//
//	for (final QueryParameter parameter : tokens.getParameters()) {
//	    result.add(parameter);
//	}
//
//	for (final QueryModel queryModel : tokens.getQueryModels()) {
//	    result.addAll(queryModel.getAllParams());
//	}
//
//	return Collections.unmodifiableList(result);
//    }
//
//    private Map<String, Object> getNamedParamsValues() {
//	final Map<String, Object> result = new HashMap<String, Object>();
//
//	// populate to result all params names/values that exist within query and all its subqueries
//	result.putAll(parametersValues);
//
//	for (final QueryModel queryModel : tokens.getQueryModels()) {
//	    result.putAll(queryModel.getNamedParamsValues());
//	}
//
//	return Collections.unmodifiableMap(result);
//    }

    public List<String> getYieldedPropsNames() {
//	final List<String> result = new ArrayList<String>();
//	for (final SelectCalculatedProperty prop : tokens.getSelect().getSelectCalculatedProps()) {
//	    result.add(prop.getPropertyAlias());
//	}
	// TODO
	return null;//result;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Derived models related routines ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

//    @Override
//    public PrimitiveResultQueryModel getCountModel() {
//	//final IQueryModel<EntityAggregates> countModelBase = new QueryModel<EntityAggregates>(tokens.clonAsUnorderedModel());
//	final PrimitiveResultQueryModel countModel = select(countModelBase).yield().Exp("COUNT(*)", "RECORDS_COUNT");
//
//	((QueryModel) countModel).parametersValues.putAll(parametersValues); // copying parameters set by user
//	return countModel;
//    }

//    @Override
//    public IQueryOrderedModel<AbstractEntity> getModelWithAbstractEntities() {
//	return new QueryModel<AbstractEntity>(this.tokens);
//    }
//
//    public AliasNumerator getAliasNumerator() {
//	return aliasNumerator;
//    }
//
//    public void setAliasNumerator(final AliasNumerator aliasNumerator) {
//	this.aliasNumerator = aliasNumerator;
//    }

    public boolean isLightweight() {
	return lightweight;
    }

    public void setLightweight(final boolean lightweight) {
	this.lightweight = lightweight;
    }

//    public void enhanceWith(final IFilter filter, final String userName) {
//	if (filter != null) {
//	    tokens.enhanceWith(filter, userName);
//	}
//    }

    private SqlQueryModel getMasterModel() {
        return masterModel;
    }

    private void setMasterModel(final SqlQueryModel masterModel) {
        this.masterModel = masterModel;
    }

    public List<Pair<TokenCategory, Object>> getTokens() {
        return tokens;
    }
}