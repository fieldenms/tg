package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.*;

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
    public ISingleOperand<IYieldOperandConcatOfSeparator<T, ET>, ET> concatOf() {
        return new SingleOperand<>(builder.concatOf()) {
            @Override
            protected IYieldOperandConcatOfSeparator<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
                return new IYieldOperandConcatOfSeparator<T, ET>() {
                    @Override
                    public IYieldOperandConcatOfSeparatorOperand<T, ET> separator() {
                        return new YieldOperandConcatOfSeparatorOperand<T, ET>(builder.separator()) {
                            @Override
                            protected T nextForYieldOperandConcatOfSeparator(final EqlSentenceBuilder builder) {
                                return YieldedItem.this.nextForSingleOperand(builder);
                            }
                        };
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
