package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.util.Collection;

import static java.util.Arrays.asList;

abstract class MultipleOperand<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<T, ET> //
		implements IMultipleOperand<T, ET> {

	protected MultipleOperand(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public T anyOfProps(final Collection<String> propertyNames) {
		return nextForSingleOperand(builder.anyOfProps(propertyNames));
	}

	public T anyOfProps(final String... propertyNames) {
		return anyOfProps(asList(propertyNames));
	}

	@Override
	public T anyOfProps(final IConvertableToPath... propertyNames) {
		return nextForSingleOperand(builder.anyOfProps(propertyNames));
	}

	@Override
	public T anyOfValues(final Collection<?> values) {
		return nextForSingleOperand(builder.anyOfValues(values));
	}

	public T anyOfValues(final Object... values) {
		return anyOfValues(asList(values));
	}

	@Override
	public T anyOfParams(final Collection<String> paramNames) {
		return nextForSingleOperand(builder.anyOfParams(paramNames));
	}

	public T anyOfParams(final String... paramNames) {
		return anyOfParams(asList(paramNames));
	}

	@Override
	public T anyOfIParams(final Collection<String> paramNames) {
		return nextForSingleOperand(builder.anyOfIParams(paramNames));
	}

	public T anyOfIParams(final String... paramNames) {
		return anyOfIParams(asList(paramNames));
	}

	@Override
	public T anyOfModels(final Collection<? extends PrimitiveResultQueryModel> models) {
		return nextForSingleOperand(builder.anyOfModels(models));
	}

	public T anyOfModels(final PrimitiveResultQueryModel... models) {
		return anyOfModels(asList(models));
	}

	@Override
	public T anyOfExpressions(final Collection<? extends ExpressionModel> expressions) {
		return nextForSingleOperand(builder.anyOfExpressions(expressions));
	}

	public T anyOfExpressions(final ExpressionModel... expressions) {
		return anyOfExpressions(asList(expressions));
	}

	@Override
	public T allOfProps(final Collection<String> propertyNames) {
		return nextForSingleOperand(builder.allOfProps(propertyNames));
	}

	public T allOfProps(final String... propertyNames) {
		return allOfProps(asList(propertyNames));
	}

	public T allOfProps(final IConvertableToPath... propertyNames) {
		return nextForSingleOperand(builder.allOfProps(propertyNames));
	}

	@Override
	public T allOfValues(final Collection<?> values) {
		return nextForSingleOperand(builder.allOfValues(values));
	}

	public T allOfValues(final Object... values) {
		return allOfValues(asList(values));
	}

	@Override
	public T allOfParams(final Collection<String> paramNames) {
		return nextForSingleOperand(builder.allOfParams(paramNames));
	}

	public T allOfParams(final String... paramNames) {
		return allOfParams(asList(paramNames));
	}

	@Override
	public T allOfIParams(final Collection<String> paramNames) {
		return nextForSingleOperand(builder.allOfIParams(paramNames));
	}

	public T allOfIParams(final String... paramNames) {
		return allOfIParams(asList(paramNames));
	}

	@Override
	public T allOfModels(final Collection<? extends PrimitiveResultQueryModel> models) {
		return nextForSingleOperand(builder.allOfModels(models));
	}

	public T allOfModels(final PrimitiveResultQueryModel... models) {
		return allOfModels(asList(models));
	}

	@Override
	public T allOfExpressions(final Collection<? extends ExpressionModel> expressions) {
		return nextForSingleOperand(builder.allOfExpressions(expressions));
	}

	public T allOfExpressions(final ExpressionModel... expressions) {
		return allOfExpressions(asList(expressions));
	}

}
