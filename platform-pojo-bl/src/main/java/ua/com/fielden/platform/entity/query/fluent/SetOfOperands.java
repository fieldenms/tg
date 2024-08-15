package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

abstract class SetOfOperands<T, ET extends AbstractEntity<?>> //
        extends SingleOperand<T, ET> //
        implements IComparisonSetOperand<T> {

    protected SetOfOperands(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public <E extends Object> T values(final E... values) {
        if (values.length == 0) {
            throw new EqlException("At least one value is expected when calling [values].");
        } else {
            return nextForSingleOperand(builder.setOfValues(values));
        }
    }

    @Override
    public T props(final String... properties) {
        return nextForSingleOperand(builder.setOfProps(properties));
    }

    @Override
    public T props(final IConvertableToPath... properties) {
        return nextForSingleOperand(builder.setOfProps(properties));
    }

    @Override
    public T params(final String... paramNames) {
        return nextForSingleOperand(builder.setOfParams(paramNames));
    }

    @Override
    public T iParams(final String... paramNames) {
        return nextForSingleOperand(builder.setOfIParams(paramNames));
    }

    @Override
    public T model(final SingleResultQueryModel model) {
        return nextForSingleOperand(builder.model(model));
    }

}
