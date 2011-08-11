package ua.com.fielden.platform.equery;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.tokens.conditions.BetweenCondition;
import ua.com.fielden.platform.equery.tokens.conditions.ComparisonOperation;
import ua.com.fielden.platform.equery.tokens.conditions.ComparisonWithoutArgumentOperation;
import ua.com.fielden.platform.equery.tokens.conditions.Condition;
import ua.com.fielden.platform.equery.tokens.conditions.ConditionWithoutArgument;
import ua.com.fielden.platform.equery.tokens.conditions.ExistsCondition;
import ua.com.fielden.platform.equery.tokens.conditions.GroupCondition;
import ua.com.fielden.platform.equery.tokens.conditions.ImplicitOrGroupCondition;
import ua.com.fielden.platform.equery.tokens.conditions.ImplicitOrInWithModelsCondition;
import ua.com.fielden.platform.equery.tokens.conditions.InCondition;
import ua.com.fielden.platform.equery.tokens.conditions.PropertyToPropertyCondition;
import ua.com.fielden.platform.equery.tokens.main.ConditionsGroup;
import ua.com.fielden.platform.equery.tokens.main.GroupBy;
import ua.com.fielden.platform.equery.tokens.main.JoinConditions;
import ua.com.fielden.platform.equery.tokens.main.OrderBy;
import ua.com.fielden.platform.equery.tokens.main.QuerySource;
import ua.com.fielden.platform.equery.tokens.main.Select;
import ua.com.fielden.platform.equery.tokens.properties.GroupByProperty;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;
import ua.com.fielden.platform.equery.tokens.properties.SelectCalculatedProperty;

/**
 * Represents some part of query that has to be generated as a result of one arbitrary method added via chaining during query crafting.
 *
 * @author TG Team
 *
 */
public final class QueryTokens {

    private Select select = new Select();
    private ConditionsGroup where = new ConditionsGroup(null);
    private GroupBy groupBy = new GroupBy();
    private OrderBy orderBy = new OrderBy();

    private List<JoinConditions> joins = new ArrayList<JoinConditions>();
    private Class resultType;

    private transient ConditionsGroup currCondGroup;// IS JUST REFERENCE to either top level group (i.e. <where>), or any other group of arbitrary nested level.
    private transient ConditionsGroup currOnGroup = new ConditionsGroup();

    public QueryTokens() {
    }

    ConditionsGroup getCurrCondOrOnGroup() {
	return currOnGroup != null ? currOnGroup : currCondGroup;
    }

    @Override
    public String toString() {
	// TODO Auto-generated method stub
	return select + "\n" + where;
    }

    public List<String> getResultantPropertiesAliases() {
	final List<String> result = new ArrayList<String>();
	for (final SelectCalculatedProperty property : select.getSelectCalculatedProps()) {
	    result.add(property.getPropertyAlias());
	    // TODO should not put here aliases of properties that are entities themselves - why?
	}

	return result;
    }

    public List<QueryParameter> getParameters() {
	final List<QueryParameter> parameters = new ArrayList<QueryParameter>();

	for (final Condition condition : where.getConditions()) {
	    parameters.addAll(getParameters(condition));
	}

	for (final SelectCalculatedProperty property : select.getSelectCalculatedProps()) {
	    parameters.addAll(property.getParams());
	}

	for (final GroupByProperty property : groupBy.getProperties()) {
	    parameters.addAll(property.getParams());
	}

	return parameters;
    }

    private List<QueryParameter> getParameters(final Condition condition) {
	final List<QueryParameter> parameters = new ArrayList<QueryParameter>();

	if (condition instanceof PropertyToPropertyCondition || condition instanceof BetweenCondition) {
	    parameters.addAll(condition.getPropsParams());
	} else if (condition instanceof InCondition) {
	    parameters.add(((InCondition) condition).getParameter());
	} else if (condition instanceof ImplicitOrGroupCondition) {
	    parameters.addAll(((ImplicitOrGroupCondition) condition).getParams());
	} else if (condition instanceof GroupCondition) {
	    for (final Condition groupCondition : ((GroupCondition) condition).getGroup().getConditions()) {
		parameters.addAll(getParameters(groupCondition));
	    }
	}

	return parameters;
    }

    public List<IQueryModel<? extends AbstractEntity>> getSubQueries() {
	final List<IQueryModel<? extends AbstractEntity>> subQueries = new ArrayList<IQueryModel<? extends AbstractEntity>>();

	// TODO add others models in the future -- correlated subqueries in plain-condition statements

	subQueries.addAll(getConditionSubQueries(where.getConditions()));

	for (final SelectCalculatedProperty property : select.getSelectCalculatedProps()) {
	    subQueries.addAll(property.getModels());
	}

	for (final GroupByProperty property : groupBy.getProperties()) {
	    subQueries.addAll(property.getModels());
	}

	return subQueries;
    }

    private List<IQueryModel<? extends AbstractEntity>> getConditionSubQueries(final List<Condition> conditions) {
	final List<IQueryModel<? extends AbstractEntity>> subQueries = new ArrayList<IQueryModel<? extends AbstractEntity>>();
	for (final Condition condition : conditions) {
	    if (condition instanceof ExistsCondition) {
		subQueries.add(((ExistsCondition) condition).getModel());
	    } else if (condition instanceof ImplicitOrInWithModelsCondition) {
		subQueries.addAll(((ImplicitOrInWithModelsCondition) condition).getModels());
	    } else if (condition instanceof GroupCondition) {
		subQueries.addAll(getConditionSubQueries(((GroupCondition) condition).getGroup().getConditions()));
	    } else if (condition instanceof PropertyToPropertyCondition || condition instanceof BetweenCondition) {
		subQueries.addAll(condition.getPropsModels());
	    }
	}

	//subQueries.addAll(select.getSelectSubqueries().values()); //TODO consider potential usecase

	return subQueries;

    }


    //////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// CLONING ROUTINES ///////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////

    private ConditionsGroup findCurrent(final ConditionsGroup initialGroup) {
	final List<ConditionsGroup> result = new ArrayList<ConditionsGroup>();
	if (initialGroup.isCurrent()) {
	    result.add(initialGroup);
	    // return initialGroup;
	} else if (initialGroup.getCurrGroupCondition() instanceof GroupCondition) {
	    final ConditionsGroup nested = findCurrent(initialGroup.getCurrGroupCondition().getGroup());
	    if (nested != null) {
		result.add(nested);
		// return nested;
	    }
	}

	for (final Condition condition : initialGroup.getConditions()) {
	    if (condition instanceof GroupCondition) {
		final ConditionsGroup nested = findCurrent(((GroupCondition) condition).getGroup());
		if (nested != null) {
		    result.add(nested);
		    //return nested;
		}
	    }
	}

	if (result.size() == 0) {
	    return null;
	} else if (result.size() == 1) {
	    return result.get(0);
	} else {
	    throw new RuntimeException("find multiple current groups: " + result.size());
	}
	//return null;
    }

    private void markConditionsGroupAsCurrent(final ConditionsGroup becomesCurrent) {
	if (currCondGroup != null) {
	    currCondGroup.setCurrent(false);
	}
	currCondGroup = becomesCurrent;
	currCondGroup.setCurrent(true);
    }

    private List<JoinConditions> clonJoins() {
	final List<JoinConditions> cloned = new ArrayList<JoinConditions>();
	for (final JoinConditions join : joins) {
	    cloned.add(join.clon());
	}
	return cloned;
    }

    private QueryTokens clonBasics() {
	final QueryTokens clonedTokens = new QueryTokens();
	clonedTokens.select = select.clon();
	clonedTokens.resultType = resultType;
	clonedTokens.groupBy = groupBy.clon();
	clonedTokens.joins = clonJoins(); // TODO
	clonedTokens.currOnGroup = currOnGroup; // TODO

	return clonedTokens;
    }

    public QueryTokens clon() {
	final QueryTokens clonedTokens = clonBasics();
	clonedTokens.where = where.clon();
	clonedTokens.orderBy = orderBy.clon();
	clonedTokens.currCondGroup = findCurrent(clonedTokens.where);
	return clonedTokens;
    }

    QueryTokens clonAsUnorderedModel() {
	final QueryTokens clonedTokens = clonBasics();
	clonedTokens.select = select.clonWithoutSubqueries();
	clonedTokens.where = where.clon();
	clonedTokens.currCondGroup = findCurrent(clonedTokens.where);
	return clonedTokens;
    }

    // TODO implement equals

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// OPERATIONS //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private QueryTokens processCondition(final Condition condition) {
	getCurrCondOrOnGroup().pushCurrCondition(condition);
	return this;
    }

    private QueryTokens cancelCondition() {
	getCurrCondOrOnGroup().resetCurrentValues();
	return this;
    }

    QueryTokens and() {
	getCurrCondOrOnGroup().setCurrLogicalOperator(LogicalOperator.AND);
	return this;
    }

    QueryTokens or() {
	getCurrCondOrOnGroup().setCurrLogicalOperator(LogicalOperator.OR);
	return this;
    }

    QueryTokens openParenthesis(final boolean negated) {
	final GroupCondition newGroup = new GroupCondition(currCondGroup.getCurrLogicalOperator(), negated, this.currCondGroup);
	currCondGroup.setCurrGroupCondition(newGroup);
	markConditionsGroupAsCurrent(newGroup.getGroup());
	return this;
    }

    QueryTokens closeParenthesis() {
	if (this.currCondGroup.getParentGroupReference() != null) {
	    markConditionsGroupAsCurrent(currCondGroup.getParentGroupReference());
	} else {
	    markConditionsGroupAsCurrent(where);
	}

	if (currCondGroup.getCurrGroupCondition().getGroup().getConditions().size() > 0) {
	    currCondGroup.pushCurrCondition(currCondGroup.getCurrGroupCondition());
	} else {
	    cancelCondition();
	}

	return this;
    }

    QueryTokens exists(final boolean negated, final IQueryModel<? extends AbstractEntity> model) {
	return processCondition(new ExistsCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), negated, model));
    }

    QueryTokens isTrue() {
	return processCondition(new ConditionWithoutArgument(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), ComparisonWithoutArgumentOperation.IS_TRUE));
    }

    QueryTokens isFalse() {
	return processCondition(new ConditionWithoutArgument(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), ComparisonWithoutArgumentOperation.IS_FALSE));
    }

    QueryTokens isNull() {
	return processCondition(new ConditionWithoutArgument(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), ComparisonWithoutArgumentOperation.IS_NULL));
    }

    QueryTokens isNotNull() {
	return processCondition(new ConditionWithoutArgument(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), ComparisonWithoutArgumentOperation.IS_NOT_NULL));
    }

    QueryTokens lt() {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.LT);
	return this;
    }

    QueryTokens gt() {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.GT);
	return this;
    }

    QueryTokens le() {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.LE);
	return this;
    }

    QueryTokens ge() {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.GE);
	return this;
    }

    QueryTokens eq() {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.EQ);
	return this;
    }

    QueryTokens ne() {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.NE);
	return this;
    }

    QueryTokens like(final boolean negated) {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.LIKE);
	getCurrCondOrOnGroup().setCurrNegated(negated);
	return this;
    }

    QueryTokens in(final boolean negated) {
	getCurrCondOrOnGroup().setCurrOperation(ComparisonOperation.IN);
	getCurrCondOrOnGroup().setCurrNegated(negated);
	return this;
    }

    QueryTokens prop(final String propertyName){
	getCurrCondOrOnGroup().setCurrProperty(new SearchProperty("[" + propertyName + "]"));
	return this;
    }

    QueryTokens exp(final String expression, final Object... values){
	getCurrCondOrOnGroup().setCurrProperty(new SearchProperty(expression, values));
	return this;
    }
    QueryTokens val(final Object value){
	getCurrCondOrOnGroup().setCurrProperty(new SearchProperty("[1]", value));
	return this;
    }
    QueryTokens model(final IQueryModel model){
	getCurrCondOrOnGroup().setCurrProperty(new SearchProperty("[1]", model));
	return this;
    }
    QueryTokens param(final String paramName){
	getCurrCondOrOnGroup().setCurrProperty(new SearchProperty("[:" + paramName +"]"));
	return this;
    }

    QueryTokens rightProp(final String propertyName){
	return processCondition(new PropertyToPropertyCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), new SearchProperty("[" + propertyName +"]"), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrOperation()));
    }

    QueryTokens rightExp(final String expression, final Object... values){
	return processCondition(new PropertyToPropertyCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), new SearchProperty(expression, values), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrOperation()));
    }

    QueryTokens rightParam(final String paramName){
	if (getCurrCondOrOnGroup().getCurrOperation().equals(ComparisonOperation.IN)) {
	    return processCondition(new InCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrProperty(), new QueryParameter(paramName, null)));
	} else {
	    return processCondition(new PropertyToPropertyCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), new SearchProperty("[:" + paramName + "]"), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrOperation()));
	}
    }

    QueryTokens rightVal(final Object... values) {
	if (getCurrCondOrOnGroup().getCurrOperation().equals(ComparisonOperation.IN)) {
	    if (values.length > 0) {
		return processCondition(new InCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrProperty(), new QueryParameter(null, values)));
	    } else {
		return cancelCondition();
	    }
	} else {
	    if (values == null || values.length == 0) {
		return cancelCondition();
	    } else if (values.length == 1) {
		if (values[0] != null) {
		    return processCondition(new PropertyToPropertyCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), new SearchProperty("[1]", values[0]), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrOperation()));
		} else {
		    return cancelCondition(); //throw new RuntimeException("value is null!"); //return isNotNull();
		}
	    } else { // when values.length > 1
		return processCondition(new ImplicitOrGroupCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), getCurrCondOrOnGroup().getCurrOperation(), values));
	    }
	}
    }

    QueryTokens rightModel(final IQueryModel... models) {
	if (getCurrCondOrOnGroup().getCurrOperation().equals(ComparisonOperation.IN)) {
	    if (models != null && models.length > 0) {
		return processCondition(new ImplicitOrInWithModelsCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrProperty(), models));
	    } else {
		return cancelCondition();
	    }
	} else {
	    // TODO
	    return null; //processCondition(new PlainCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), getCurrCondOrOnGroup().isCurrNegated(), getCurrCondOrOnGroup().getCurrOperation(), new QueryParameter(paramName, null)));
	}
    }

    QueryTokens between(final Object value1, final Object value2) {
	if (value1 != null && value2 == null) {
	    return processCondition(new PropertyToPropertyCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), new SearchProperty("[1]", value1), getCurrCondOrOnGroup().isCurrNegated(), ComparisonOperation.GE));
	} else if (value1 == null && value2 != null) {
	    return processCondition(new PropertyToPropertyCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), new SearchProperty("[1]", value2), getCurrCondOrOnGroup().isCurrNegated(), ComparisonOperation.LE));
	} else if (value1 != null && value2 != null) {
	    return processCondition(new BetweenCondition(getCurrCondOrOnGroup().getCurrLogicalOperator(), getCurrCondOrOnGroup().getCurrProperty(), ComparisonOperation.BETWEEN, new SearchProperty("[1]", value1),new SearchProperty("[1]", value2)));
	} else {
	    return cancelCondition();
	}
    }

    QueryTokens groupByProp(final String property) {
	groupBy.getProperties().add(new GroupByProperty("[" + property + "]"));
	return this;
    }

    QueryTokens groupByExp(final String property, final Object...values) {
	groupBy.getProperties().add(new GroupByProperty(property, values));
	return this;
    }

    QueryTokens orderBy(final String... otherProperties) {
	orderBy.add(otherProperties);
	return this;
    }

    QueryTokens yieldExp(final String propertyExpression, final String alias, final Object...values) {
	select.getSelectCalculatedProps().add(new SelectCalculatedProperty(propertyExpression, alias, values));
	return this;
    }

    QueryTokens yieldProp(final String property, final String alias) {
	select.getSelectCalculatedProps().add(new SelectCalculatedProperty("[" + property + "]", alias));
	return this;
    }

    QueryTokens yieldProp(final String property) {
	if (property.contains(".")) {
	    throw new RuntimeException("Dot.notated properties should always have explicit alias provided!");
	}
	return yieldProp(property, property);
    }

    QueryTokens yieldValue(final Object value, final String alias) {
	select.getSelectCalculatedProps().add(new SelectCalculatedProperty("[1]", alias, value));
	return this;
    }

    <E extends AbstractEntity> QueryTokens yieldModel(final IQueryModel<? extends E> subModel, final String alias) {
	select.getSelectCalculatedProps().add(new SelectCalculatedProperty("[1]", alias, subModel));
	return this;
    }

    <E extends AbstractEntity> QueryTokens resultType(final Class<E> resultType) {
	this.resultType = resultType;
	return this;
    }

    <E extends AbstractEntity> QueryTokens from(final Class<E> entityType, final String alias) {
	this.joins.add(new JoinConditions(new QuerySource(entityType), alias));
	return this;
    }

    QueryTokens from(final String alias, final IQueryModel... sourceModels) {
	this.joins.add(new JoinConditions(new QuerySource(sourceModels), alias));
	return this;
    }

    <E extends AbstractEntity> QueryTokens join(final Class<E> entityType, final String alias) {
	currOnGroup = new ConditionsGroup();
	joins.add(new JoinConditions(new QuerySource(entityType), alias, false, currOnGroup));
	return this;
    }

    <E extends AbstractEntity> QueryTokens leftJoin(final Class<E> entityType, final String alias) {
	currOnGroup = new ConditionsGroup();
	joins.add(new JoinConditions(new QuerySource(entityType), alias, true, currOnGroup));
	return this;
    }

    <E extends AbstractEntity> QueryTokens join(final IQueryModel model, final String alias) {
	currOnGroup = new ConditionsGroup();
	joins.add(new JoinConditions(new QuerySource(model), alias, false, currOnGroup));
	return this;
    }

    <E extends AbstractEntity> QueryTokens leftJoin(final IQueryModel model, final String alias) {
	currOnGroup = new ConditionsGroup();
	joins.add(new JoinConditions(new QuerySource(model), alias, true, currOnGroup));
	return this;
    }

    QueryTokens where() {
	currOnGroup = null;
	return this;
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// GETTERS //////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    Class getResultType() {
	return resultType != null ? resultType
		: joins.size() == 1 && joins.get(0).getQuerySource().isEntityTypeBased() && select.noPropertiesSpecified() ? joins.get(0).getQuerySource().getEntityType() : null;
    }

    Select getSelect() {
	return select;
    }

    ConditionsGroup getWhere() {
	return where;
    }

    GroupBy getGroupBy() {
	return groupBy;
    }

    OrderBy getOrderBy() {
	return orderBy;
    }

    List<JoinConditions> getJoins() {
	return joins;
    }
}
