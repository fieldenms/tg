package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionTo;
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

    protected SingleOperand(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForSingleOperand(final Tokens tokens);

	@Override
	public T val(final Object value) {
		return nextForSingleOperand(getTokens().val(value));
	}

	@Override
	public T iVal(final Object value) {
		return nextForSingleOperand(getTokens().iVal(value));
	}

	@Override
	public T model(final SingleResultQueryModel<?> model) {
		return nextForSingleOperand(getTokens().model(model));
	}

	@Override
	public T param(final String paramName) {
		return nextForSingleOperand(getTokens().param(paramName));
	}

	@Override
	public T param(final Enum paramName) {
		return param(paramName.toString());
	}

	@Override
	public T iParam(final String paramName) {
		return nextForSingleOperand(getTokens().iParam(paramName));
	}

	@Override
	public T iParam(final Enum paramName) {
		return iParam(paramName.toString());
	}

	@Override
	public T prop(final String propertyName) {
		return nextForSingleOperand(getTokens().prop(propertyName));
	}

	@Override
	public T prop(final Enum propertyName) {
		return prop(propertyName.toString());
	}

	@Override
	public T extProp(final String propertyName) {
		return nextForSingleOperand(getTokens().extProp(propertyName));
	}

	@Override
	public T extProp(final Enum propertyName) {
		return extProp(propertyName.toString());
	}

	@Override
	public T expr(final ExpressionModel expr) {
		return nextForSingleOperand(getTokens().expr(expr));
	}

	@Override
	public IDateAddIntervalFunctionArgument<T, ET> addIntervalOf() {
		return createDateAddIntervalFunctionArgument(getTokens().addDateInterval());
	}

	@Override
	public IDateDiffIntervalFunction<T, ET> count() {
		return createDateDiffIntervalFunction(getTokens().countDateIntervalFunction());
	}

	@Override
	public IFunctionWhere0<T, ET> caseWhen() {
		return createFunctionWhere0(getTokens().caseWhenFunction());
	}

	@Override
	public IIfNullFunctionArgument<T, ET> ifNull() {
		return createIfNullFunctionArgument(getTokens().ifNull());
	}

	@Override
	public IConcatFunctionArgument<T, ET> concat() {
		return createConcatFunctionArgument(getTokens().concat());
	}

	@Override
	public IRoundFunctionArgument<T, ET> round() {
		return createRoundFunctionArgument(getTokens().round());
	}

	@Override
	public T now() {
		return nextForSingleOperand(getTokens().now());
	}

	@Override
	public IFunctionLastArgument<T, ET> upperCase() {
		return createFunctionLastArgument(getTokens().uppercase());
	}

	@Override
	public IFunctionLastArgument<T, ET> lowerCase() {
		return createFunctionLastArgument(getTokens().lowercase());
	}

	@Override
	public IFunctionLastArgument<T, ET> secondOf() {
		return createFunctionLastArgument(getTokens().secondOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> minuteOf() {
		return createFunctionLastArgument(getTokens().minuteOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> hourOf() {
		return createFunctionLastArgument(getTokens().hourOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> dayOf() {
		return createFunctionLastArgument(getTokens().dayOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> monthOf() {
		return createFunctionLastArgument(getTokens().monthOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> yearOf() {
		return createFunctionLastArgument(getTokens().yearOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> dateOf() {
		return createFunctionLastArgument(getTokens().dateOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> absOf() {
		return createFunctionLastArgument(getTokens().absOf());
	}
	
	private IDateAddIntervalFunctionArgument<T, ET> createDateAddIntervalFunctionArgument(final Tokens tokens) {
		return new DateAddIntervalFunctionArgument<T, ET>(tokens) {

			@Override
			protected T nextForDateAddIntervalFunctionArgument(Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}

		};
	}

	private DateDiffIntervalFunction<T, ET> createDateDiffIntervalFunction(final Tokens tokens) {
		return new DateDiffIntervalFunction<T, ET>(tokens) {

			@Override
			protected T nextForDateDiffIntervalFunction(final Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}

		};
	}

	private FunctionWhere0<T, ET> createFunctionWhere0(final Tokens tokens) {
		return new FunctionWhere0<T, ET>(tokens) {

			@Override
			protected T nextForFunctionWhere0(final Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}

		};
	}

	private IfNullFunctionArgument<T, ET> createIfNullFunctionArgument(final Tokens tokens) {
		return new IfNullFunctionArgument<T, ET>(tokens) {

			@Override
			protected T nextForIfNullFunctionArgument(final Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}

		};
	}

	private ConcatFunctionArgument<T, ET> createConcatFunctionArgument(final Tokens tokens) {
		return new ConcatFunctionArgument<T, ET>(tokens) {

			@Override
			protected T nextForConcatFunctionArgument(final Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}

		};
	}

	private RoundFunctionArgument<T, ET> createRoundFunctionArgument(final Tokens tokens) {
		return new RoundFunctionArgument<T, ET>(tokens) {

			@Override
			protected T nextForRoundFunctionArgument(final Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}

		};
	}

	protected FunctionLastArgument<T, ET> createFunctionLastArgument(final Tokens tokens) {
		return new FunctionLastArgument<T, ET>(tokens) {

			@Override
			protected T nextForFunctionLastArgument(final Tokens tokens) {
				return SingleOperand.this.nextForSingleOperand(tokens);
			}
		};
	}
}