package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperand;

abstract class AbstractYieldExprOperand<T1, T2> extends AbstractYieldedItem<T1> implements IYieldExprOperand<T1, T2> {

    protected AbstractYieldExprOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T2 beginExpr() {
	getTokens().beginExpression();
	return getParent2();
    }

    abstract T2 getParent2();
}
