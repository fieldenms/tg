package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperandConcatOfNext;

abstract class YieldedItem<T, ET extends AbstractEntity<?>> //
        extends SingleOperand<T, ET> //
        implements IYieldOperand<T, ET> {

    public YieldedItem(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IFunctionLastArgument<T, ET> maxOf() {
        return createFunctionLastArgument(builder.maxOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> minOf() {
        return createFunctionLastArgument(builder.minOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> sumOf() {
        return createFunctionLastArgument(builder.sumOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> countOf() {
        return createFunctionLastArgument(builder.countOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> avgOf() {
        return createFunctionLastArgument(builder.averageOf());
    }

    @Override
    public ISingleOperand<IYieldOperandConcatOfNext<T, ET>, ET> concatOf() {
        return new SingleOperand<>(builder.concatOf()) {
            @Override
            protected IYieldOperandConcatOfNext<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
                return new YieldOperandConcatOfNext<>(builder) {
                    @Override
                    protected T nextForYieldOperandConcatOfNext(final EqlSentenceBuilder builder) {
                        return YieldedItem.this.nextForSingleOperand(builder);
                    }
                };
            }
        };
    }

    @Override
    public IFunctionLastArgument<T, ET> sumOfDistinct() {
        return createFunctionLastArgument(builder.sumOfDistinct());
    }

    @Override
    public IFunctionLastArgument<T, ET> countOfDistinct() {
        return createFunctionLastArgument(builder.countOfDistinct());
    }

    @Override
    public IFunctionLastArgument<T, ET> avgOfDistinct() {
        return createFunctionLastArgument(builder.averageOfDistinct());
    }

    @Override
    public T countAll() {
        return nextForSingleOperand(builder.countAll());
    }

}
