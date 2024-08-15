package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class SingleOperand<T, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements ISingleOperand<T, ET> {

    protected SingleOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForSingleOperand(final EqlSentenceBuilder builder);

    @Override
    public T val(final Object value) {
        return nextForSingleOperand(builder.val(value));
    }

    @Override
    public T iVal(final Object value) {
        return nextForSingleOperand(builder.iVal(value));
    }

    @Override
    public T model(final SingleResultQueryModel<?> model) {
        return nextForSingleOperand(builder.model(model));
    }

    @Override
    public T param(final String paramName) {
        return nextForSingleOperand(builder.param(paramName));
    }

    @Override
    public T param(final Enum paramName) {
        return param(paramName.toString());
    }

    @Override
    public T iParam(final String paramName) {
        return nextForSingleOperand(builder.iParam(paramName));
    }

    @Override
    public T iParam(final Enum paramName) {
        return iParam(paramName.toString());
    }

    @Override
    public T prop(final CharSequence propertyName) {
        return nextForSingleOperand(builder.prop(propertyName));
    }

    @Override
    public T prop(final Enum propertyName) {
        return prop(propertyName.toString());
    }

    @Override
    public T extProp(final CharSequence propertyName) {
        return nextForSingleOperand(builder.extProp(propertyName));
    }

    @Override
    public T extProp(final Enum propertyName) {
        return extProp(propertyName.toString());
    }

    @Override
    public T expr(final ExpressionModel expr) {
        return nextForSingleOperand(builder.expr(expr));
    }

    @Override
    public IDateAddIntervalFunctionArgument<T, ET> addTimeIntervalOf() {
        return createDateAddIntervalFunctionArgument(builder.addDateInterval());
    }

    @Override
    public IDateDiffIntervalFunction<T, ET> count() {
        return createDateDiffIntervalFunction(builder.countDateIntervalFunction());
    }

    @Override
    public IFunctionWhere0<T, ET> caseWhen() {
        return createFunctionWhere0(builder.caseWhenFunction());
    }

    @Override
    public IIfNullFunctionArgument<T, ET> ifNull() {
        return createIfNullFunctionArgument(builder.ifNull());
    }

    @Override
    public IConcatFunctionArgument<T, ET> concat() {
        return createConcatFunctionArgument(builder.concat());
    }

    @Override
    public IRoundFunctionArgument<T, ET> round() {
        return createRoundFunctionArgument(builder.round());
    }

    @Override
    public T now() {
        return nextForSingleOperand(builder.now());
    }

    @Override
    public IFunctionLastArgument<T, ET> upperCase() {
        return createFunctionLastArgument(builder.uppercase());
    }

    @Override
    public IFunctionLastArgument<T, ET> lowerCase() {
        return createFunctionLastArgument(builder.lowercase());
    }

    @Override
    public IFunctionLastArgument<T, ET> secondOf() {
        return createFunctionLastArgument(builder.secondOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> minuteOf() {
        return createFunctionLastArgument(builder.minuteOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> hourOf() {
        return createFunctionLastArgument(builder.hourOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> dayOf() {
        return createFunctionLastArgument(builder.dayOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> monthOf() {
        return createFunctionLastArgument(builder.monthOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> yearOf() {
        return createFunctionLastArgument(builder.yearOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> dayOfWeekOf() {
        return createFunctionLastArgument(builder.dayOfWeekOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> dateOf() {
        return createFunctionLastArgument(builder.dateOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> absOf() {
        return createFunctionLastArgument(builder.absOf());
    }

    private IDateAddIntervalFunctionArgument<T, ET> createDateAddIntervalFunctionArgument(final EqlSentenceBuilder builder) {
        return new DateAddIntervalFunctionArgument<T, ET>(builder) {

            @Override
            protected T nextForDateAddIntervalFunctionArgument(EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }

        };
    }

    private DateDiffIntervalFunction<T, ET> createDateDiffIntervalFunction(final EqlSentenceBuilder builder) {
        return new DateDiffIntervalFunction<T, ET>(builder) {

            @Override
            protected T nextForDateDiffIntervalFunction(final EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }

        };
    }

    private FunctionWhere0<T, ET> createFunctionWhere0(final EqlSentenceBuilder builder) {
        return new FunctionWhere0<T, ET>(builder) {

            @Override
            protected T nextForFunctionWhere0(final EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }

        };
    }

    private IfNullFunctionArgument<T, ET> createIfNullFunctionArgument(final EqlSentenceBuilder builder) {
        return new IfNullFunctionArgument<T, ET>(builder) {

            @Override
            protected T nextForIfNullFunctionArgument(final EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }

        };
    }

    private ConcatFunctionArgument<T, ET> createConcatFunctionArgument(final EqlSentenceBuilder builder) {
        return new ConcatFunctionArgument<T, ET>(builder) {

            @Override
            protected T nextForConcatFunctionArgument(final EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }

        };
    }

    private RoundFunctionArgument<T, ET> createRoundFunctionArgument(final EqlSentenceBuilder builder) {
        return new RoundFunctionArgument<T, ET>(builder) {

            @Override
            protected T nextForRoundFunctionArgument(final EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }

        };
    }

    protected FunctionLastArgument<T, ET> createFunctionLastArgument(final EqlSentenceBuilder builder) {
        return new FunctionLastArgument<T, ET>(builder) {

            @Override
            protected T nextForFunctionLastArgument(final EqlSentenceBuilder builder) {
                return SingleOperand.this.nextForSingleOperand(builder);
            }
        };
    }

}
