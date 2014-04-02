package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonQuantifiedOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonSetOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class AbstractComparisonOperator<T extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
extends AbstractQueryLink //
implements IComparisonOperator<T, ET> {
    abstract T getParent1();

    IComparisonOperand<T, ET> getParent2() {
        return new AbstractExpConditionalOperand<T, ET>(getTokens()) {
            @Override
            T getParent() {
                return getParent1();
            }
        };
    }

    IComparisonSetOperand<T> getParent3() {
        return new AbstractSetOfOperands<T, ET>(getTokens()) {
            @Override
            T getParent() {
                return getParent1();
            }
        };
    }

    IComparisonQuantifiedOperand<T, ET> getParent4() {
        return new AbstractExpRightSideConditionalOperand<T, ET>(getTokens()) {
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
    public IComparisonQuantifiedOperand<T, ET> eq() {
        return copy(getParent4(), getTokens().eq());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> ne() {
        return copy(getParent4(), getTokens().ne());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> ge() {
        return copy(getParent4(), getTokens().ge());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> le() {
        return copy(getParent4(), getTokens().le());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> gt() {
        return copy(getParent4(), getTokens().gt());
    }

    @Override
    public IComparisonQuantifiedOperand<T, ET> lt() {
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
    public IComparisonOperand<T, ET> like() {
        return copy(getParent2(), getTokens().like(false));
    }

    @Override
    public IComparisonOperand<T, ET> notLike() {
        return copy(getParent2(), getTokens().like(true));
    }

    @Override
    public IComparisonOperand<T, ET> iLike() {
        return copy(getParent2(), getTokens().iLike(false));
    }

    @Override
    public IComparisonOperand<T, ET> notILike() {
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