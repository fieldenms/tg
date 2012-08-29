package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

public class StandAloneConditionOperand<ET extends AbstractEntity<?>> //<ET extends AbstractEntity<?>> //
//extends AbstractConditionalOperand<IComparisonOperand<IStandAloneConditionComparisonOperator<>, ET>, IExistenceOperator<IStandAloneConditionCompoundCondition>, ET> //
extends AbstractQueryLink
implements IStandAloneConditionOperand<ET> {

//    IStandAloneExprOperationAndClose getParent1() {
//	return new StandAloneConditionComparisonOperator(getTokens());
//    }
//
    protected StandAloneConditionOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> anyOfProps(final String... propertyNames) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().anyOfProps(propertyNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> anyOfValues(final Object... values) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().anyOfValues(values));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> anyOfParams(final String... paramNames) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().anyOfParams(paramNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> anyOfModels(final PrimitiveResultQueryModel... models) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().anyOfModels(models));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> anyOfExpressions(final ExpressionModel... expressions) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().anyOfExpressions(expressions));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> allOfProps(final String... propertyNames) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().allOfProps(propertyNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> allOfValues(final Object... values) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().allOfValues(values));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> allOfParams(final String... paramNames) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().allOfParams(paramNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> allOfModels(final PrimitiveResultQueryModel... models) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().allOfModels(models));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> allOfExpressions(final ExpressionModel... expressions) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().allOfExpressions(expressions));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> prop(final String propertyName) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().prop(propertyName));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> extProp(final String propertyName) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().extProp(propertyName));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> val(final Object value) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().val(value));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> iVal(final Object value) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().iVal(value));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> param(final String paramName) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().param(paramName));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> iParam(final String paramName) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().iParam(paramName));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> model(final SingleResultQueryModel<?> model) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().model(model));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> expr(final ExpressionModel expr) {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().expr(expr));
    }

    @Override
    public IStandAloneConditionComparisonOperator<ET> now() {
	return new StandAloneConditionComparisonOperator<ET>(getTokens().now());
    }


    @Override
    public IStandAloneConditionCompoundCondition<ET> condition(final ConditionModel condition) {
	return new StandAloneConditionCompoundCondition<ET>(getTokens().cond(condition));
    }

    @Override
    public IDateDiffIntervalFunction<IStandAloneConditionComparisonOperator<ET>, ET> count() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> upperCase() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> lowerCase() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> secondOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> minuteOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> hourOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> dayOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> monthOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator<ET>, ET> yearOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IIfNullFunctionArgument<IStandAloneConditionComparisonOperator<ET>, ET> ifNull() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionWhere0<IStandAloneConditionComparisonOperator<ET>, ET> caseWhen() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IRoundFunctionArgument<IStandAloneConditionComparisonOperator<ET>, ET> round() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IConcatFunctionArgument<IStandAloneConditionComparisonOperator<ET>, ET> concat() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IExprOperand0<IStandAloneConditionComparisonOperator<ET>, ET> beginExpr() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition<ET> exists(final QueryModel subQuery) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition<ET> notExists(final QueryModel subQuery) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition<ET> existsAnyOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition<ET> notExistsAnyOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition<ET> existsAllOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition<ET> notExistsAllOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }
}
