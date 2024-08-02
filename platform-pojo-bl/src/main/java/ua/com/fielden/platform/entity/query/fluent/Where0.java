package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class Where0<ET extends AbstractEntity<?>> //
        extends Where<IComparisonOperator0<ET>, ICompoundCondition0<ET>, IWhere1<ET>, ET> //
        implements IWhere0<ET> {

    protected Where0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IWhere1<ET> nextForWhere(final EqlSentenceBuilder builder) {
        return new Where1<ET>(builder);
    }

    @Override
    protected ICompoundCondition0<ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new CompoundCondition0<ET>(builder);
    }

    @Override
    protected IComparisonOperator0<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new ComparisonOperator0<ET>(builder);
    }

}
