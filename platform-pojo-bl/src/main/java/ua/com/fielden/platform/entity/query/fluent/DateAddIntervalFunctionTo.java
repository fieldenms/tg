package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionTo;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class DateAddIntervalFunctionTo<T, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements IDateAddIntervalFunctionTo<T, ET> {

    protected DateAddIntervalFunctionTo(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForDateAddIntervalFunctionTo(final EqlSentenceBuilder builder);

    @Override
    public IFunctionLastArgument<T, ET> to() {
        return createFunctionLastArgument(builder.to());
    }

    private FunctionLastArgument<T, ET> createFunctionLastArgument(final EqlSentenceBuilder builder) {
        return new FunctionLastArgument<T, ET>(builder) {

            @Override
            protected T nextForFunctionLastArgument(final EqlSentenceBuilder builder) {
                return DateAddIntervalFunctionTo.this.nextForDateAddIntervalFunctionTo(builder);
            }

        };
    }

}
