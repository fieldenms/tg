package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class AbstractMultipleOperand<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<T, ET> implements IMultipleOperand<T, ET> {

    @Override
    public T anyOfProps(final String... propertyNames) {
        return copy(nextForAbstractSingleOperand(), getTokens().anyOfProps(propertyNames));
    }

    @Override
    public T anyOfValues(final Object... values) {
        return copy(nextForAbstractSingleOperand(), getTokens().anyOfValues(values));
    }

    @Override
    public T anyOfParams(final String... paramNames) {
        return copy(nextForAbstractSingleOperand(), getTokens().anyOfParams(paramNames));
    }

    @Override
    public T anyOfIParams(final String... paramNames) {
        return copy(nextForAbstractSingleOperand(), getTokens().anyOfIParams(paramNames));
    }

    @Override
    public T anyOfModels(final PrimitiveResultQueryModel... models) {
        return copy(nextForAbstractSingleOperand(), getTokens().anyOfModels(models));
    }

    @Override
    public T anyOfExpressions(final ExpressionModel... expressions) {
        return copy(nextForAbstractSingleOperand(), getTokens().anyOfExpressions(expressions));
    }

    @Override
    public T allOfProps(final String... propertyNames) {
        return copy(nextForAbstractSingleOperand(), getTokens().allOfProps(propertyNames));
    }

    @Override
    public T allOfValues(final Object... values) {
        return copy(nextForAbstractSingleOperand(), getTokens().allOfValues(values));
    }

    @Override
    public T allOfParams(final String... paramNames) {
        return copy(nextForAbstractSingleOperand(), getTokens().allOfParams(paramNames));
    }

    @Override
    public T allOfIParams(final String... paramNames) {
        return copy(nextForAbstractSingleOperand(), getTokens().allOfIParams(paramNames));
    }

    @Override
    public T allOfModels(final PrimitiveResultQueryModel... models) {
        return copy(nextForAbstractSingleOperand(), getTokens().allOfModels(models));
    }

    @Override
    public T allOfExpressions(final ExpressionModel... expressions) {
        return copy(nextForAbstractSingleOperand(), getTokens().allOfExpressions(expressions));
    }
}