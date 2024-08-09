package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;

public final class StandAloneExpOperand //
        extends YieldedItem<IStandAloneExprOperationAndClose, AbstractEntity<?>> //
        implements IStandAloneExprOperand {

    public StandAloneExpOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IStandAloneExprOperationAndClose nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new StandAloneExpOperationAndClose(builder);
    }

}
