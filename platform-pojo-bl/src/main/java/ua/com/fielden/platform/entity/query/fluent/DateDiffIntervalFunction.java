package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;

abstract class DateDiffIntervalFunction<T, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements IDateDiffIntervalFunction<T, ET> {

    protected DateDiffIntervalFunction(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForDateDiffIntervalFunction(final EqlSentenceBuilder builder);

    @Override
    public IDateDiffFunction<T, ET> seconds() {
        return createDateDiffFunction(builder.secondsInterval());
    }

    @Override
    public IDateDiffFunction<T, ET> minutes() {
        return createDateDiffFunction(builder.minutesInterval());
    }

    @Override
    public IDateDiffFunction<T, ET> hours() {
        return createDateDiffFunction(builder.hoursInterval());
    }

    @Override
    public IDateDiffFunction<T, ET> days() {
        return createDateDiffFunction(builder.daysInterval());
    }

    @Override
    public IDateDiffFunction<T, ET> months() {
        return createDateDiffFunction(builder.monthsInterval());
    }

    @Override
    public IDateDiffFunction<T, ET> years() {
        return createDateDiffFunction(builder.yearsInterval());
    }

    private DateDiffFunction<T, ET> createDateDiffFunction(final EqlSentenceBuilder builder) {
        return new DateDiffFunction<T, ET>(builder) {

            @Override
            protected T nextForDateDiffFunction(final EqlSentenceBuilder builder) {
                return DateDiffIntervalFunction.this.nextForDateDiffIntervalFunction(builder);
            }

        };
    }

}
