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

abstract class SingleOperand<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements ISingleOperand<T, ET> {

	protected abstract T nextForSingleOperand();

	@Override
	public T val(final Object value) {
		return copy(nextForSingleOperand(), getTokens().val(value));
	}

	@Override
	public T iVal(final Object value) {
		return copy(nextForSingleOperand(), getTokens().iVal(value));
	}

	@Override
	public T model(final SingleResultQueryModel<?> model) {
		return copy(nextForSingleOperand(), getTokens().model(model));
	}

	@Override
	public T param(final String paramName) {
		return copy(nextForSingleOperand(), getTokens().param(paramName));
	}

	@Override
	public T param(final Enum paramName) {
		return param(paramName.toString());
	}

	@Override
	public T iParam(final String paramName) {
		return copy(nextForSingleOperand(), getTokens().iParam(paramName));
	}

	@Override
	public T iParam(final Enum paramName) {
		return iParam(paramName.toString());
	}

	@Override
	public T prop(final String propertyName) {
		return copy(nextForSingleOperand(), getTokens().prop(propertyName));
	}

	@Override
	public T prop(final Enum propertyName) {
		return prop(propertyName.toString());
	}

	@Override
	public T extProp(final String propertyName) {
		return copy(nextForSingleOperand(), getTokens().extProp(propertyName));
	}

	@Override
	public T extProp(final Enum propertyName) {
		return extProp(propertyName.toString());
	}

	@Override
	public T expr(final ExpressionModel expr) {
		return copy(nextForSingleOperand(), getTokens().expr(expr));
	}

	@Override
	public IDateDiffIntervalFunction<T, ET> count() {
		return copy(createDateDiffIntervalFunction(), getTokens().countDateIntervalFunction());
	}

	@Override
	public IFunctionWhere0<T, ET> caseWhen() {
		return copy(createFunctionWhere0(), getTokens().caseWhenFunction());
	}

	@Override
	public IIfNullFunctionArgument<T, ET> ifNull() {
		return copy(createIfNullFunctionArgument(), getTokens().ifNull());
	}

	@Override
	public IConcatFunctionArgument<T, ET> concat() {
		return copy(createConcatFunctionArgument(), getTokens().concat());
	}

	@Override
	public IRoundFunctionArgument<T, ET> round() {
		return copy(createRoundFunctionArgument(), getTokens().round());
	}

	@Override
	public T now() {
		return copy(nextForSingleOperand(), getTokens().now());
	}

	@Override
	public IFunctionLastArgument<T, ET> upperCase() {
		return copy(createFunctionLastArgument(), getTokens().uppercase());
	}

	@Override
	public IFunctionLastArgument<T, ET> lowerCase() {
		return copy(createFunctionLastArgument(), getTokens().lowercase());
	}

	@Override
	public IFunctionLastArgument<T, ET> secondOf() {
		return copy(createFunctionLastArgument(), getTokens().secondOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> minuteOf() {
		return copy(createFunctionLastArgument(), getTokens().minuteOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> hourOf() {
		return copy(createFunctionLastArgument(), getTokens().hourOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> dayOf() {
		return copy(createFunctionLastArgument(), getTokens().dayOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> monthOf() {
		return copy(createFunctionLastArgument(), getTokens().monthOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> yearOf() {
		return copy(createFunctionLastArgument(), getTokens().yearOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> dateOf() {
		return copy(createFunctionLastArgument(), getTokens().dateOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> absOf() {
		return copy(createFunctionLastArgument(), getTokens().absOf());
	}
	
	private DateDiffIntervalFunction<T, ET> createDateDiffIntervalFunction() {
		return new DateDiffIntervalFunction<T, ET>() {

			@Override
			protected T nextForDateDiffIntervalFunction() {
				return SingleOperand.this.nextForSingleOperand();
			}

		};
	}

	private FunctionWhere0<T, ET> createFunctionWhere0() {
		return new FunctionWhere0<T, ET>() {

			@Override
			protected T nextForFunctionWhere0() {
				return SingleOperand.this.nextForSingleOperand();
			}

		};
	}

	private IfNullFunctionArgument<T, ET> createIfNullFunctionArgument() {
		return new IfNullFunctionArgument<T, ET>() {

			@Override
			protected T nextForIfNullFunctionArgument() {
				return SingleOperand.this.nextForSingleOperand();
			}

		};
	}

	private ConcatFunctionArgument<T, ET> createConcatFunctionArgument() {
		return new ConcatFunctionArgument<T, ET>() {

			@Override
			protected T nextForConcatFunctionArgument() {
				return SingleOperand.this.nextForSingleOperand();
			}

		};
	}

	private RoundFunctionArgument<T, ET> createRoundFunctionArgument() {
		return new RoundFunctionArgument<T, ET>() {

			@Override
			protected T nextForRoundFunctionArgument() {
				return SingleOperand.this.nextForSingleOperand();
			}

		};
	}

	protected FunctionLastArgument<T, ET> createFunctionLastArgument() {
		return new FunctionLastArgument<T, ET>() {

			@Override
			protected T nextForFunctionLastArgument() {
				return SingleOperand.this.nextForSingleOperand();
			}
		};
	}
}