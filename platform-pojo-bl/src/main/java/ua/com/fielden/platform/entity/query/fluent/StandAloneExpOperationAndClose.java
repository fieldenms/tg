package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public final class StandAloneExpOperationAndClose //
		extends ArithmeticalOperator<IStandAloneExprOperand> //
		implements IStandAloneExprOperationAndClose {

	@Override
	public ExpressionModel model() {
		return new ExpressionModel(getTokens().getValues());
	}

	@Override
	protected IStandAloneExprOperand nextForAbstractArithmeticalOperator() {
		return new StandAloneExpOperand();
	}
}