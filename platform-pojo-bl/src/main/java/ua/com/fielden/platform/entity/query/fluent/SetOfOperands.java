package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.util.Collection;

import static java.util.Arrays.asList;

abstract class SetOfOperands<T, ET extends AbstractEntity<?>> //
        extends SingleOperand<T, ET> //
        implements IComparisonSetOperand<T> {

    protected SetOfOperands(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public T values(final Collection<?> values) {
        if (values.isEmpty()) {
            throw new EqlException("At least one value is expected when calling [values].");
        } else {
            return nextForSingleOperand(builder.setOfValues(values));
        }
    }

    @Override
    public <E extends Object> T values(final E... values) {
        return values(asList(values));
    }

    @Override
    public T props(final Collection<? extends CharSequence> properties) {
        return nextForSingleOperand(builder.setOfProps(properties));
    }

    public T props(final CharSequence... properties) {
        return nextForSingleOperand(builder.setOfProps(properties));
    }

    @Override
    public T params(final Collection<? extends CharSequence> paramNames) {
        return nextForSingleOperand(builder.setOfParams(paramNames));
    }

    public T params(final CharSequence... paramNames) {
        return params(asList(paramNames));
    }

    @Override
    public T iParams(final Collection<? extends CharSequence> paramNames) {
        return nextForSingleOperand(builder.setOfIParams(paramNames));
    }

    public T iParams(final CharSequence... paramNames) {
        return iParams(asList(paramNames));
    }

    @Override
    public T model(final SingleResultQueryModel model) {
        return nextForSingleOperand(builder.model(model));
    }

}
