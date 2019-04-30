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
	
    protected ComparisonOperator(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForComparisonOperator(final Tokens tokens);

	@Override
	public IComparisonQuantifiedOperand<T, ET> eq() {
		return createIComparisonQuantifiedOperand(getTokens().eq());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> ne() {
		return createIComparisonQuantifiedOperand(getTokens().ne());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> ge() {
		return createIComparisonQuantifiedOperand(getTokens().ge());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> le() {
		return createIComparisonQuantifiedOperand(getTokens().le());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> gt() {
		return createIComparisonQuantifiedOperand(getTokens().gt());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> lt() {
		return createIComparisonQuantifiedOperand(getTokens().lt());
	}

	@Override
	public IComparisonSetOperand<T> in() {
		return createIComparisonSetOperand(getTokens().in(false));
	}

	@Override
	public IComparisonSetOperand<T> notIn() {
		return createIComparisonSetOperand(getTokens().in(true));
	}

	@Override
	public IComparisonOperand<T, ET> like() {
		return createIComparisonOperand(getTokens().like(options().build()));
	}

	@Override
	public IComparisonOperand<T, ET> notLike() {
		return createIComparisonOperand(getTokens().like(options().negated().build()));
	}

	@Override
	public IComparisonOperand<T, ET> iLike() {
		return createIComparisonOperand(getTokens().like(options().caseInsensitive().build()));
	}

	@Override
	public IComparisonOperand<T, ET> notILike() {
		return createIComparisonOperand(getTokens().like(options().caseInsensitive().negated().build()));
	}

    @Override
    public IComparisonOperand<T, ET> likeWithCast() {
        return createIComparisonOperand(getTokens().like(options().withCast().build()));
    }

    @Override
    public IComparisonOperand<T, ET> notLikeWithCast() {
        return createIComparisonOperand(getTokens().like(options().withCast().negated().build()));
    }

    @Override
    public IComparisonOperand<T, ET> iLikeWithCast() {
        return createIComparisonOperand(getTokens().like(options().caseInsensitive().withCast().build()));
    }

    @Override
    public IComparisonOperand<T, ET> notILikeWithCast() {
        return createIComparisonOperand(getTokens().like(options().caseInsensitive().negated().withCast().build()));
    }
	
    @Override
    public T isNull() {
        return nextForComparisonOperator(getTokens().isNull(false));
    }

    @Override
    public T isNotNull() {
        return nextForComparisonOperator(getTokens().isNull(true));
    }

    private IComparisonOperand<T, ET> createIComparisonOperand(final Tokens tokens) {
        return new ExpConditionalOperand<T, ET>(tokens) {
            @Override
            protected T nextForSingleOperand(final Tokens tokens) {
                return ComparisonOperator.this.nextForComparisonOperator(tokens);
            }
        };
    }

    private IComparisonSetOperand<T> createIComparisonSetOperand(final Tokens tokens) {
        return new SetOfOperands<T, ET>(tokens) {
            @Override
            protected T nextForSingleOperand(final Tokens tokens) {
                return ComparisonOperator.this.nextForComparisonOperator(tokens);
            }
        };
    }

    private IComparisonQuantifiedOperand<T, ET> createIComparisonQuantifiedOperand(final Tokens tokens) {
        return new ExpRightSideConditionalOperand<T, ET>(tokens) {
            @Override
            protected T nextForSingleOperand(final Tokens tokens) {
                return ComparisonOperator.this.nextForComparisonOperator(tokens);
            }
        };
    }
}