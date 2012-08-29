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

public class StandAloneConditionOperand //<ET extends AbstractEntity<?>> //
//extends AbstractConditionalOperand<IComparisonOperand<IStandAloneConditionComparisonOperator<>, ET>, IExistenceOperator<IStandAloneConditionCompoundCondition>, ET> //
extends AbstractQueryLink
implements IStandAloneConditionOperand {
    protected StandAloneConditionOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IStandAloneConditionComparisonOperator anyOfProps(final String... propertyNames) {
	return new StandAloneConditionComparisonOperator(getTokens().anyOfProps(propertyNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator anyOfValues(final Object... values) {
	return new StandAloneConditionComparisonOperator(getTokens().anyOfValues(values));
    }

    @Override
    public IStandAloneConditionComparisonOperator anyOfParams(final String... paramNames) {
	return new StandAloneConditionComparisonOperator(getTokens().anyOfParams(paramNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator anyOfModels(final PrimitiveResultQueryModel... models) {
	return new StandAloneConditionComparisonOperator(getTokens().anyOfModels(models));
    }

    @Override
    public IStandAloneConditionComparisonOperator anyOfExpressions(final ExpressionModel... expressions) {
	return new StandAloneConditionComparisonOperator(getTokens().anyOfExpressions(expressions));
    }

    @Override
    public IStandAloneConditionComparisonOperator allOfProps(final String... propertyNames) {
	return new StandAloneConditionComparisonOperator(getTokens().allOfProps(propertyNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator allOfValues(final Object... values) {
	return new StandAloneConditionComparisonOperator(getTokens().allOfValues(values));
    }

    @Override
    public IStandAloneConditionComparisonOperator allOfParams(final String... paramNames) {
	return new StandAloneConditionComparisonOperator(getTokens().allOfParams(paramNames));
    }

    @Override
    public IStandAloneConditionComparisonOperator allOfModels(final PrimitiveResultQueryModel... models) {
	return new StandAloneConditionComparisonOperator(getTokens().allOfModels(models));
    }

    @Override
    public IStandAloneConditionComparisonOperator allOfExpressions(final ExpressionModel... expressions) {
	return new StandAloneConditionComparisonOperator(getTokens().allOfExpressions(expressions));
    }

    @Override
    public IStandAloneConditionComparisonOperator prop(final String propertyName) {
	return new StandAloneConditionComparisonOperator(getTokens().prop(propertyName));
    }

    @Override
    public IStandAloneConditionComparisonOperator extProp(final String propertyName) {
	return new StandAloneConditionComparisonOperator(getTokens().extProp(propertyName));
    }

    @Override
    public IStandAloneConditionComparisonOperator val(final Object value) {
	return new StandAloneConditionComparisonOperator(getTokens().val(value));
    }

    @Override
    public IStandAloneConditionComparisonOperator iVal(final Object value) {
	return new StandAloneConditionComparisonOperator(getTokens().iVal(value));
    }

    @Override
    public IStandAloneConditionComparisonOperator param(final String paramName) {
	return new StandAloneConditionComparisonOperator(getTokens().param(paramName));
    }

    @Override
    public IStandAloneConditionComparisonOperator iParam(final String paramName) {
	return new StandAloneConditionComparisonOperator(getTokens().iParam(paramName));
    }

    @Override
    public IStandAloneConditionComparisonOperator model(final SingleResultQueryModel<?> model) {
	return new StandAloneConditionComparisonOperator(getTokens().model(model));
    }

    @Override
    public IStandAloneConditionComparisonOperator expr(final ExpressionModel expr) {
	return new StandAloneConditionComparisonOperator(getTokens().expr(expr));
    }

    @Override
    public IStandAloneConditionComparisonOperator now() {
	return new StandAloneConditionComparisonOperator(getTokens().now());
    }

    @Override
    public IDateDiffIntervalFunction<IStandAloneConditionComparisonOperator, AbstractEntity<?>> count() {
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> upperCase() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> lowerCase() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> secondOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> minuteOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> hourOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> dayOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> monthOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionLastArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> yearOf() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IIfNullFunctionArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> ifNull() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IFunctionWhere0<IStandAloneConditionComparisonOperator, AbstractEntity<?>> caseWhen() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IRoundFunctionArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> round() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IConcatFunctionArgument<IStandAloneConditionComparisonOperator, AbstractEntity<?>> concat() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IExprOperand0<IStandAloneConditionComparisonOperator, AbstractEntity<?>> beginExpr() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition exists(final QueryModel subQuery) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition notExists(final QueryModel subQuery) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition existsAnyOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition notExistsAnyOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition existsAllOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition notExistsAllOf(final QueryModel... subQueries) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IStandAloneConditionCompoundCondition condition(final ConditionModel condition) {
	return new StandAloneConditionCompoundCondition(getTokens().cond(condition));
    }
}
