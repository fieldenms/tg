package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class AbstractSingleOperand<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements ISingleOperand<T, ET> {
    abstract T getParent();

    protected AbstractSingleOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T val(final Object value) {
	return copy(getParent(), getTokens().val(value));
    }

    @Override
    public T iVal(final Object value) {
	return copy(getParent(), getTokens().iVal(value));
    }

    @Override
    public T model(final SingleResultQueryModel<?> model) {
	return copy(getParent(), getTokens().model(model));
    }

    @Override
    public T param(final String paramName) {
	return copy(getParent(), getTokens().param(paramName));
    }

    @Override
    public T iParam(final String paramName) {
	return copy(getParent(), getTokens().iParam(paramName));
    }

    @Override
    public T prop(final String propertyName) {
	return copy(getParent(), getTokens().prop(propertyName));
    }

    @Override
    public T extProp(final String propertyName) {
	return copy(getParent(), getTokens().extProp(propertyName));
    }

    @Override
    public T expr(final ExpressionModel expr) {
	return copy(getParent(), getTokens().expr(expr));
    }

    @Override
    public IDateDiffIntervalFunction<T, ET> count() {
	return new DateDiffIntervalFunction<T, ET>(getTokens().countDateIntervalFunction(), getParent());
    }

    @Override
    public IFunctionWhere0<T, ET> caseWhen() {
	return new FunctionWhere0<T, ET>(getTokens().caseWhenFunction(), getParent());
    }

    @Override
    public IIfNullFunctionArgument<T, ET> ifNull() {
	return new IfNullFunctionArgument<T, ET>(getTokens().ifNull(), getParent());
    }

    @Override
    public IConcatFunctionArgument<T, ET> concat() {
	return new ConcatFunctionArgument<T, ET>(getTokens().concat(), getParent());
    }

    @Override
    public IRoundFunctionArgument<T, ET> round() {
	return new RoundFunctionArgument<T, ET>(getTokens().round(), getParent());
    }

    @Override
    public T now() {
	return copy(getParent(), getTokens().now());
    }

    @Override
    public IFunctionLastArgument<T, ET> upperCase() {
	return new FunctionLastArgument<T, ET>(getTokens().uppercase(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> lowerCase() {
	return new FunctionLastArgument<T, ET>(getTokens().lowercase(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> secondOf() {
	return new FunctionLastArgument<T, ET>(getTokens().secondOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> minuteOf() {
	return new FunctionLastArgument<T, ET>(getTokens().minuteOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> hourOf() {
	return new FunctionLastArgument<T, ET>(getTokens().hourOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> dayOf() {
	return new FunctionLastArgument<T, ET>(getTokens().dayOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> monthOf() {
	return new FunctionLastArgument<T, ET>(getTokens().monthOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> yearOf() {
	return new FunctionLastArgument<T, ET>(getTokens().yearOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> dateOf() {
	return new FunctionLastArgument<T, ET>(getTokens().dateOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> absOf() {
	return new FunctionLastArgument<T, ET>(getTokens().absOf(), getParent());
    }

}