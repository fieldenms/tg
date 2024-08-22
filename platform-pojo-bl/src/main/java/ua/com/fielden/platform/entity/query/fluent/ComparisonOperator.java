package ua.com.fielden.platform.entity.query.fluent;

import static ua.com.fielden.platform.entity.query.fluent.LikeOptions.options;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class ComparisonOperator<T extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements IComparisonOperator<T, ET> {

    protected ComparisonOperator(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForComparisonOperator(final EqlSentenceBuilder builder);

    @Override
    public IComparisonQuantifiedOperand<T, ET> eq() {
        return createIComparisonQuantifiedOperand(builder.eq());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> ne() {
        return createIComparisonQuantifiedOperand(builder.ne());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> ge() {
        return createIComparisonQuantifiedOperand(builder.ge());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> le() {
        return createIComparisonQuantifiedOperand(builder.le());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> gt() {
        return createIComparisonQuantifiedOperand(builder.gt());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> lt() {
        return createIComparisonQuantifiedOperand(builder.lt());
    }

    @Override
    public IComparisonSetOperand<T> in() {
        return createIComparisonSetOperand(builder.in(false));
    }

    @Override
    public IComparisonSetOperand<T> notIn() {
        return createIComparisonSetOperand(builder.in(true));
    }

    @Override
    public IComparisonOperand<T, ET> like() {
        return createIComparisonOperand(builder.like());
    }

    @Override
    public IComparisonOperand<T, ET> notLike() {
        return createIComparisonOperand(builder.notLike());
    }

    @Override
    public IComparisonOperand<T, ET> iLike() {
        return createIComparisonOperand(builder.iLike());
    }

    @Override
    public IComparisonOperand<T, ET> notILike() {
        return createIComparisonOperand(builder.notILike());
    }

    @Override
    public IComparisonOperand<T, ET> likeWithCast() {
        return createIComparisonOperand(builder.likeWithCast());
    }

    @Override
    public IComparisonOperand<T, ET> notLikeWithCast() {
        return createIComparisonOperand(builder.notLikeWithCast());
    }

    @Override
    public IComparisonOperand<T, ET> iLikeWithCast() {
        return createIComparisonOperand(builder.iLikeWithCast());
    }

    @Override
    public IComparisonOperand<T, ET> notILikeWithCast() {
        return createIComparisonOperand(builder.notILikeWithCast());
    }

    @Override
    public T isNull() {
        return nextForComparisonOperator(builder.isNull(false));
    }

    @Override
    public T isNotNull() {
        return nextForComparisonOperator(builder.isNull(true));
    }

    private IComparisonOperand<T, ET> createIComparisonOperand(final EqlSentenceBuilder builder) {
        return new ExpConditionalOperand<T, ET>(builder) {
            @Override
            protected T nextForSingleOperand(final EqlSentenceBuilder builder) {
                return ComparisonOperator.this.nextForComparisonOperator(builder);
            }
        };
    }

    private IComparisonSetOperand<T> createIComparisonSetOperand(final EqlSentenceBuilder builder) {
        return new SetOfOperands<T, ET>(builder) {
            @Override
            protected T nextForSingleOperand(final EqlSentenceBuilder builder) {
                return ComparisonOperator.this.nextForComparisonOperator(builder);
            }
        };
    }

    private IComparisonQuantifiedOperand<T, ET> createIComparisonQuantifiedOperand(final EqlSentenceBuilder builder) {
        return new ExpRightSideConditionalOperand<T, ET>(builder) {
            @Override
            protected T nextForSingleOperand(final EqlSentenceBuilder builder) {
                return ComparisonOperator.this.nextForComparisonOperator(builder);
            }
        };
    }

}
