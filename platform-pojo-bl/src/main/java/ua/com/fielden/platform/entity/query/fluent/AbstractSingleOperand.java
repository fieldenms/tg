package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class AbstractSingleOperand<T> extends AbstractQueryLink implements ISingleOperand<T> {
    abstract T getParent();

    protected AbstractSingleOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T val(final Object value) {
	getTokens().val(value);
	return getParent();
    }

    @Override
    public T iVal(final Object value) {
	getTokens().iVal(value);
	return getParent();
    }

    @Override
    public <E extends AbstractEntity> T model(final PrimitiveResultQueryModel model) {
	getTokens().model(model);
	return getParent();
    }

    @Override
    public T param(final String paramName) {
	getTokens().param(paramName);
	return getParent();
    }

    @Override
    public T iParam(final String paramName) {
	getTokens().iParam(paramName);
	return getParent();
    }

    @Override
    public T prop(final String propertyName) {
	getTokens().prop(propertyName);
	return getParent();
    }

    @Override
    public T expr(final ExpressionModel expr) {
	getTokens().expr(expr);
	return getParent();
    }

    @Override
    public IDateDiffFunction<T> countDays() {
	return new DateDiffFunction<T>(getTokens().countDaysFunction(), getParent());
    }

    @Override
    public IFunctionWhere0<T> caseWhen() {
	return new FunctionWhere0<T>(getTokens().caseWhenFunction(), getParent());
    }

    @Override
    public IIfNullFunctionArgument<T> ifNull() {
	return new IfNullFunctionArgument<T>(getTokens().ifNull(), getParent());
    }

    @Override
    public IRoundFunctionArgument<T> round() {
	return new RoundFunctionArgument<T>(getTokens().round(), getParent());
    }

    @Override
    public T now() {
	getTokens().now();
	return getParent();
    }

    @Override
    public IFunctionLastArgument<T> upperCase() {
	return new FunctionLastArgument<T>(getTokens().uppercase(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> lowerCase() {
	return new FunctionLastArgument<T>(getTokens().lowercase(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> secondOf() {
	return new FunctionLastArgument<T>(getTokens().secondOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> minuteOf() {
	return new FunctionLastArgument<T>(getTokens().minuteOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> hourOf() {
	return new FunctionLastArgument<T>(getTokens().hourOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> dayOf() {
	return new FunctionLastArgument<T>(getTokens().dayOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> monthOf() {
	return new FunctionLastArgument<T>(getTokens().monthOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> yearOf() {
	return new FunctionLastArgument<T>(getTokens().yearOf(), getParent());
    }
}
