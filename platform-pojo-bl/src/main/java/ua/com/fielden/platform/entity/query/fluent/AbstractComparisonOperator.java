package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;


abstract class AbstractComparisonOperator<T extends ILogicalOperator<?>> extends AbstractQueryLink implements IComparisonOperator<T> {
    abstract T getParent1();

    //@Override
    IComparisonOperand<T> getParent2() {
	return new AbstractExpConditionalOperand<T>(getTokens()) {
	    @Override
	    T getParent() {
		return getParent1();
	    }
	};
    }

    //@Override
    IComparisonSetOperand<T> getParent3() {
	return new AbstractSetOfOperands<T>(getTokens()) {
	    @Override
	    T getParent() {
		return getParent1();
	    }
	};
    }

    //@Override
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
	getTokens().eq();
	return getParent4();
    }

    @Override
    public IComparisonQuantifiedOperand<T> ne() {
	getTokens().ne();
	return getParent4();
    }

    @Override
    public IComparisonQuantifiedOperand<T> ge() {
	getTokens().ge();
	return getParent4();
    }

    @Override
    public IComparisonQuantifiedOperand<T> le() {
	getTokens().le();
	return getParent4();
    }

    @Override
    public IComparisonQuantifiedOperand<T> gt() {
	getTokens().gt();
	return getParent4();
    }

    @Override
    public IComparisonQuantifiedOperand<T> lt() {
	getTokens().lt();
	return getParent4();
    }

    @Override
    public IComparisonSetOperand<T> in() {
	getTokens().in(false);
	return getParent3();
    }

    @Override
    public IComparisonSetOperand<T> notIn() {
	getTokens().in(true);
	return getParent3();
    }

    @Override
    public IComparisonOperand<T> like() {
	getTokens().like(false);
	return getParent2();
    }

    @Override
    public IComparisonOperand<T> notLike() {
	getTokens().like(true);
	return getParent2();
    }

    @Override
    public IComparisonOperand<T> iLike() {
	getTokens().iLike(false);
	return getParent2();
    }

    @Override
    public IComparisonOperand<T> notILike() {
	getTokens().iLike(true);
	return getParent2();
    }

    @Override
    public T isNull() {
	getTokens().isNull(false);
	return getParent1();
    }

    @Override
    public T isNotNull() {
	getTokens().isNull(true);
	return getParent1();
    }
}
