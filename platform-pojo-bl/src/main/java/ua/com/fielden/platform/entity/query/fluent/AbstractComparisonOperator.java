package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;


abstract class AbstractComparisonOperator<T extends ILogicalOperator<?>> extends AbstractQueryLink implements IComparisonOperator<T> {
    abstract T getParent1();

    IComparisonOperand<T> getParent2() {
	return new AbstractExpConditionalOperand<T>(getTokens()) {
	    @Override
	    T getParent() {
		return getParent1();
	    }
	};
    }

    IComparisonSetOperand<T> getParent3() {
	return new AbstractSetOfOperands<T>(getTokens()) {
	    @Override
	    T getParent() {
		return getParent1();
	    }
	};
    }

    IComparisonQuantifiedOperand<T> getParent4() {
	return new AbstractExpRightSideConditionalOperand<T>(getTokens()) {
	    @Override
	    T getParent() {
		return getParent1();
	    }
	};
    }

    AbstractComparisonOperator(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IComparisonQuantifiedOperand<T> eq() {
	return copy(getParent4(), getTokens().eq());
    }

    @Override
    public IComparisonQuantifiedOperand<T> ne() {
	return copy(getParent4(), getTokens().ne());
    }

    @Override
    public IComparisonQuantifiedOperand<T> ge() {
	return copy(getParent4(), getTokens().ge());
    }

    @Override
    public IComparisonQuantifiedOperand<T> le() {
	return copy(getParent4(), getTokens().le());
    }

    @Override
    public IComparisonQuantifiedOperand<T> gt() {
	return copy(getParent4(), getTokens().gt());
    }

    @Override
    public IComparisonQuantifiedOperand<T> lt() {
	return copy(getParent4(), getTokens().lt());
    }

    @Override
    public IComparisonSetOperand<T> in() {
	return copy(getParent3(), getTokens().in(false));
    }

    @Override
    public IComparisonSetOperand<T> notIn() {
	return copy(getParent3(), getTokens().in(true));
    }

    @Override
    public IComparisonOperand<T> like() {
	return copy(getParent2(), getTokens().like(false));
    }

    @Override
    public IComparisonOperand<T> notLike() {
	return copy(getParent2(), getTokens().like(true));
    }

    @Override
    public IComparisonOperand<T> iLike() {
	return copy(getParent2(), getTokens().iLike(false));
    }

    @Override
    public IComparisonOperand<T> notILike() {
	return copy(getParent2(), getTokens().iLike(true));
    }

    @Override
    public T isNull() {
	return copy(getParent1(), getTokens().isNull(false));
    }

    @Override
    public T isNotNull() {
	return copy(getParent1(), getTokens().isNull(true));
    }
}