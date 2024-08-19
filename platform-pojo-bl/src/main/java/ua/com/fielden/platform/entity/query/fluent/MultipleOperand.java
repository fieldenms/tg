package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IMultipleOperand;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

import java.util.Collection;

import static java.util.Arrays.asList;

abstract class MultipleOperand<T, ET extends AbstractEntity<?>> //
        extends SingleOperand<T, ET> //
        implements IMultipleOperand<T, ET> {

    protected MultipleOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public T anyOfProps(final Collection<? extends CharSequence> propertyNames) {
        return nextForSingleOperand(builder.anyOfProps(propertyNames));
    }

    @Override
    public T anyOfProps(final CharSequence... propertyNames) {
        return nextForSingleOperand(builder.anyOfProps(propertyNames));
    }

    @Override
    public T anyOfValues(final Collection<?> values) {
        return nextForSingleOperand(builder.anyOfValues(values));
    }

    @Override
    public T anyOfValues(final Object... values) {
        return anyOfValues(asList(values));
    }

    @Override
    public T anyOfParams(final CharSequence... paramNames) {
        return anyOfParams(asList(paramNames));
    }

    @Override
    public T anyOfParams(final Collection<? extends CharSequence> paramNames) {
        return nextForSingleOperand(builder.anyOfParams(paramNames));
    }

    @Override
    public T anyOfIParams(final Collection<? extends CharSequence> paramNames) {
        return nextForSingleOperand(builder.anyOfIParams(paramNames));
    }

    @Override
    public T anyOfIParams(final CharSequence... paramNames) {
        return anyOfIParams(asList(paramNames));
    }

    @Override
    public T anyOfModels(final Collection<? extends PrimitiveResultQueryModel> models) {
        return nextForSingleOperand(builder.anyOfModels(models));
    }

    @Override
    public T anyOfModels(final PrimitiveResultQueryModel... models) {
        return anyOfModels(asList(models));
    }

    @Override
    public T anyOfExpressions(final Collection<? extends ExpressionModel> expressions) {
        return nextForSingleOperand(builder.anyOfExpressions(expressions));
    }

    @Override
    public T anyOfExpressions(final ExpressionModel... expressions) {
        return anyOfExpressions(asList(expressions));
    }

    @Override
    public T allOfProps(final Collection<? extends CharSequence> propertyNames) {
        return nextForSingleOperand(builder.allOfProps(propertyNames));
    }

    @Override
    public T allOfProps(final CharSequence... propertyNames) {
        return nextForSingleOperand(builder.allOfProps(propertyNames));
    }

    @Override
    public T allOfValues(final Collection<?> values) {
        return nextForSingleOperand(builder.allOfValues(values));
    }

    @Override
    public T allOfValues(final Object... values) {
        return allOfValues(asList(values));
    }

    @Override
    public T allOfParams(final Collection<? extends CharSequence> paramNames) {
        return nextForSingleOperand(builder.allOfParams(paramNames));
    }

    @Override
    public T allOfParams(final CharSequence... paramNames) {
        return allOfParams(asList(paramNames));
    }

    @Override
    public T allOfIParams(final Collection<? extends CharSequence> paramNames) {
        return nextForSingleOperand(builder.allOfIParams(paramNames));
    }

    @Override
    public T allOfIParams(final CharSequence... paramNames) {
        return allOfIParams(asList(paramNames));
    }

    @Override
    public T allOfModels(final Collection<? extends PrimitiveResultQueryModel> models) {
        return nextForSingleOperand(builder.allOfModels(models));
    }

    @Override
    public T allOfModels(final PrimitiveResultQueryModel... models) {
        return allOfModels(asList(models));
    }

    @Override
    public T allOfExpressions(final Collection<? extends ExpressionModel> expressions) {
        return nextForSingleOperand(builder.allOfExpressions(expressions));
    }

    @Override
    public T allOfExpressions(final ExpressionModel... expressions) {
        return allOfExpressions(asList(expressions));
    }

}
