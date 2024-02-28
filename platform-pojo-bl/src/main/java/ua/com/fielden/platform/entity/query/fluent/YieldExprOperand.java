package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperand;

abstract class YieldExprOperand<T1, T2, ET extends AbstractEntity<?>> //
		extends YieldedItem<T1, ET> //
		implements IYieldExprOperand<T1, T2, ET> {

	protected YieldExprOperand(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T2 nextForYieldExprOperand(final EqlSentenceBuilder builder);

	@Override
	public T2 beginExpr() {
		return nextForYieldExprOperand(builder.beginExpression());
	}

}
