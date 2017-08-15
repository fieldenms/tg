package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class StandAloneExpOperationAndClose extends AbstractArithmeticalOperator<IStandAloneExprOperand> implements IStandAloneExprOperationAndClose {

    @Override
    public ExpressionModel model() {
        return new ExpressionModel(getTokens().getValues());
    }

    @Override
    IStandAloneExprOperand nextForAbstractArithmeticalOperator() {
        return new StandAloneExpOperand();
    }
}