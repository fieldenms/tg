package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class StandAloneExpOperationAndClose extends AbstractArithmeticalOperator<IStandAloneExprOperand> implements IStandAloneExprOperationAndClose {

    protected StandAloneExpOperationAndClose(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public ExpressionModel model() {
	return new ExpressionModel(getTokens().getValues());
    }

    @Override
    IStandAloneExprOperand getParent() {
	return new StandAloneExpOperand(getTokens());
    }
}