package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;

final class ComparisonOperator1<ET extends AbstractEntity<?>> //
        extends ComparisonOperator<ICompoundCondition1<ET>, ET> //
        implements IComparisonOperator1<ET> {

    public ComparisonOperator1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected ICompoundCondition1<ET> nextForComparisonOperator(final EqlSentenceBuilder builder) {
        return new CompoundCondition1<ET>(builder);
    }

}
