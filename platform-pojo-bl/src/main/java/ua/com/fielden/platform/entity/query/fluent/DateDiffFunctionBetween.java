package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class DateDiffFunctionBetween<T, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements IDateDiffFunctionBetween<T, ET> {

    protected DateDiffFunctionBetween(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForDateDiffFunctionBetween(final EqlSentenceBuilder builder);

    @Override
    public IFunctionLastArgument<T, ET> and() {
        return createFunctionLastArgument(builder.and());
    }

    private FunctionLastArgument<T, ET> createFunctionLastArgument(final EqlSentenceBuilder builder) {
        return new FunctionLastArgument<T, ET>(builder) {

            @Override
            protected T nextForFunctionLastArgument(final EqlSentenceBuilder builder) {
                return DateDiffFunctionBetween.this.nextForDateDiffFunctionBetween(builder);
            }

        };
    }

}
