package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public final class StandAloneExpOperationAndClose //
		extends ArithmeticalOperator<IStandAloneExprOperand> //
		implements IStandAloneExprOperationAndClose {

	public StandAloneExpOperationAndClose(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public ExpressionModel model() {
		return new ExpressionModel(builder.model().getTokenSource());
	}

	@Override
	protected IStandAloneExprOperand nextForArithmeticalOperator(final EqlSentenceBuilder builder) {
		return new StandAloneExpOperand(builder);
	}

}
