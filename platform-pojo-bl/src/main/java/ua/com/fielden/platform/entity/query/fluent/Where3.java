package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where3<ET extends AbstractEntity<?>> //
        extends ConditionalOperand<IComparisonOperator3<ET>, ICompoundCondition3<ET>, ET> //
        implements IWhere3<ET> {

    public Where3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected ICompoundCondition3<ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new CompoundCondition3<ET>(builder);
    }

    @Override
    protected IComparisonOperator3<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new ComparisonOperator3<ET>(builder);
    }

}
