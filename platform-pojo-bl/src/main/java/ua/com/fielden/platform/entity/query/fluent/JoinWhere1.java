package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;

final class JoinWhere1<ET extends AbstractEntity<?>> //
        extends Where<IJoinComparisonOperator1<ET>, IJoinCompoundCondition1<ET>, IJoinWhere2<ET>, ET> //
        implements IJoinWhere1<ET> {

    public JoinWhere1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinWhere2<ET> nextForWhere(final EqlSentenceBuilder builder) {
        return new JoinWhere2<ET>(builder);
    }

    @Override
    protected IJoinCompoundCondition1<ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition1<ET>(builder);
    }

    @Override
    protected IJoinComparisonOperator1<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new JoinComparisonOperator1<ET>(builder);
    }

}
