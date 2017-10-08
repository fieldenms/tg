package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperand;

abstract class YieldExprOperand<T1, T2, ET extends AbstractEntity<?>> //
		extends YieldedItem<T1, ET> //
		implements IYieldExprOperand<T1, T2, ET> {

    protected YieldExprOperand(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T2 nextForYieldExprOperand(final Tokens tokens);

	@Override
	public T2 beginExpr() {
		return nextForYieldExprOperand(getTokens().beginExpression());
	}
}