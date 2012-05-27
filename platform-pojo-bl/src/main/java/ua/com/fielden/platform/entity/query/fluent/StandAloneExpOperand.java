package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;

public class StandAloneExpOperand extends AbstractYieldedItem<IStandAloneExprOperationAndClose, AbstractEntity<?>> implements IStandAloneExprOperand {
    protected StandAloneExpOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    IStandAloneExprOperationAndClose getParent() {
	return new StandAloneExpOperationAndClose(getTokens());
    }
}