package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class ComparisonOperator<T extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IComparisonOperator<T, ET> {
	
	protected abstract T nextForAbstractComparisonOperator();

	private IComparisonOperand<T, ET> createIComparisonOperand() {
		return new ExpConditionalOperand<T, ET>() {
			@Override
			protected T nextForAbstractSingleOperand() {
				return ComparisonOperator.this.nextForAbstractComparisonOperator();
			}
		};
	}

	private IComparisonSetOperand<T> createIComparisonSetOperand() {
		return new SetOfOperands<T, ET>() {
			@Override
			protected T nextForAbstractSingleOperand() {
				return ComparisonOperator.this.nextForAbstractComparisonOperator();
			}
		};
	}

	private IComparisonQuantifiedOperand<T, ET> createIComparisonQuantifiedOperand() {
		return new ExpRightSideConditionalOperand<T, ET>() {
			@Override
			protected T nextForAbstractSingleOperand() {
				return ComparisonOperator.this.nextForAbstractComparisonOperator();
			}
		};
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> eq() {
		return copy(createIComparisonQuantifiedOperand(), getTokens().eq());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> ne() {
		return copy(createIComparisonQuantifiedOperand(), getTokens().ne());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> ge() {
		return copy(createIComparisonQuantifiedOperand(), getTokens().ge());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> le() {
		return copy(createIComparisonQuantifiedOperand(), getTokens().le());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> gt() {
		return copy(createIComparisonQuantifiedOperand(), getTokens().gt());
	}

	@Override
	public IComparisonQuantifiedOperand<T, ET> lt() {
		return copy(createIComparisonQuantifiedOperand(), getTokens().lt());
	}

	@Override
	public IComparisonSetOperand<T> in() {
		return copy(createIComparisonSetOperand(), getTokens().in(false));
	}

	@Override
	public IComparisonSetOperand<T> notIn() {
		return copy(createIComparisonSetOperand(), getTokens().in(true));
	}

	@Override
	public IComparisonOperand<T, ET> like() {
		return copy(createIComparisonOperand(), getTokens().like(false));
	}

	@Override
	public IComparisonOperand<T, ET> notLike() {
		return copy(createIComparisonOperand(), getTokens().like(true));
	}

	@Override
	public IComparisonOperand<T, ET> iLike() {
		return copy(createIComparisonOperand(), getTokens().iLike(false));
	}

	@Override
	public IComparisonOperand<T, ET> notILike() {
		return copy(createIComparisonOperand(), getTokens().iLike(true));
	}

	@Override
	public T isNull() {
		return copy(nextForAbstractComparisonOperator(), getTokens().isNull(false));
	}

	@Override
	public T isNotNull() {
		return copy(nextForAbstractComparisonOperator(), getTokens().isNull(true));
	}
}