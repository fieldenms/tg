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
	final IComparisonQuantifiedOperand<T> result = getParent4();
	((AbstractQueryLink) result).setTokens(getTokens().eq());
	return result;
    }

    @Override
    public IComparisonQuantifiedOperand<T> ne() {
	final IComparisonQuantifiedOperand<T> result = getParent4();
	((AbstractQueryLink) result).setTokens(getTokens().ne());
	return result;
    }

    @Override
    public IComparisonQuantifiedOperand<T> ge() {
	final IComparisonQuantifiedOperand<T> result = getParent4();
	((AbstractQueryLink) result).setTokens(getTokens().ge());
	return result;
    }

    @Override
    public IComparisonQuantifiedOperand<T> le() {
	final IComparisonQuantifiedOperand<T> result = getParent4();
	((AbstractQueryLink) result).setTokens(getTokens().le());
	return result;
    }

    @Override
    public IComparisonQuantifiedOperand<T> gt() {
	final IComparisonQuantifiedOperand<T> result = getParent4();
	((AbstractQueryLink) result).setTokens(getTokens().gt());
	return result;
    }

    @Override
    public IComparisonQuantifiedOperand<T> lt() {
	final IComparisonQuantifiedOperand<T> result = getParent4();
	((AbstractQueryLink) result).setTokens(getTokens().lt());
	return result;
    }

    @Override
    public IComparisonSetOperand<T> in() {
	final IComparisonSetOperand<T> result = getParent3();
	((AbstractQueryLink) result).setTokens(getTokens().in(false));
	return result;
    }

    @Override
    public IComparisonSetOperand<T> notIn() {
	final IComparisonSetOperand<T> result = getParent3();
	((AbstractQueryLink) result).setTokens(getTokens().in(true));
	return result;
    }

    @Override
    public IComparisonOperand<T> like() {
	final IComparisonOperand<T> result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().like(false));
	return result;
    }

    @Override
    public IComparisonOperand<T> notLike() {
	final IComparisonOperand<T> result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().like(true));
	return result;
    }

    @Override
    public IComparisonOperand<T> iLike() {
	final IComparisonOperand<T> result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().iLike(false));
	return result;
    }

    @Override
    public IComparisonOperand<T> notILike() {
	final IComparisonOperand<T> result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().iLike(true));
	return result;
    }

    @Override
    public T isNull() {
	final T result = getParent1();
	((AbstractQueryLink) result).setTokens(getTokens().isNull(false));
	return result;
    }

    @Override
    public T isNotNull() {
	final T result = getParent1();
	((AbstractQueryLink) result).setTokens(getTokens().isNull(true));
	return result;
    }
}