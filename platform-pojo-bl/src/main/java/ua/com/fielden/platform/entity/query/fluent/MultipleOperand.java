package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

abstract class MultipleOperand<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<T, ET> //
		implements IMultipleOperand<T, ET> {

	protected MultipleOperand(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public T anyOfProps(final String... propertyNames) {
		return nextForSingleOperand(builder.anyOfProps(propertyNames));
	}

	@Override
	public T anyOfProps(final IConvertableToPath... propertyNames) {
		return nextForSingleOperand(builder.anyOfProps(propertyNames));
	}

	@Override
	public T anyOfValues(final Object... values) {
		return nextForSingleOperand(builder.anyOfValues(values));
	}

	@Override
	public T anyOfParams(final String... paramNames) {
		return nextForSingleOperand(builder.anyOfParams(paramNames));
	}

	@Override
	public T anyOfIParams(final String... paramNames) {
		return nextForSingleOperand(builder.anyOfIParams(paramNames));
	}

	@Override
	public T anyOfModels(final PrimitiveResultQueryModel... models) {
		return nextForSingleOperand(builder.anyOfModels(models));
	}

	@Override
	public T anyOfExpressions(final ExpressionModel... expressions) {
		return nextForSingleOperand(builder.anyOfExpressions(expressions));
	}

	@Override
	public T allOfProps(final String... propertyNames) {
		return nextForSingleOperand(builder.allOfProps(propertyNames));
	}

	@Override
	public T allOfProps(final IConvertableToPath... propertyNames) {
		return nextForSingleOperand(builder.allOfProps(propertyNames));
	}

	@Override
	public T allOfValues(final Object... values) {
		return nextForSingleOperand(builder.allOfValues(values));
	}

	@Override
	public T allOfParams(final String... paramNames) {
		return nextForSingleOperand(builder.allOfParams(paramNames));
	}

	@Override
	public T allOfIParams(final String... paramNames) {
		return nextForSingleOperand(builder.allOfIParams(paramNames));
	}

	@Override
	public T allOfModels(final PrimitiveResultQueryModel... models) {
		return nextForSingleOperand(builder.allOfModels(models));
	}

	@Override
	public T allOfExpressions(final ExpressionModel... expressions) {
		return nextForSingleOperand(builder.allOfExpressions(expressions));
	}

}
