package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;

final class JoinComparisonOperator1<ET extends AbstractEntity<?>> //
        extends ComparisonOperator<IJoinCompoundCondition1<ET>, ET> //
        implements IJoinComparisonOperator1<ET> {

    public JoinComparisonOperator1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinCompoundCondition1<ET> nextForComparisonOperator(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition1<ET>(builder);
    }

}
