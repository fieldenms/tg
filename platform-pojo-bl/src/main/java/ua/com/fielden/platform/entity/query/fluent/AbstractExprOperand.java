package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand;

abstract class AbstractExprOperand<T1, T2> extends AbstractSingleOperand<T1> implements IExprOperand<T1, T2> {
    abstract T2 getParent2();

    protected AbstractExprOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T2 beginExpr() {
	final T2 result = getParent2();
	((AbstractQueryLink) result).setTokens(getTokens().beginExpression());
	return result;
    }
}
