package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinWhere2<ET extends AbstractEntity<?>> //
        extends Where<IJoinComparisonOperator2<ET>, IJoinCompoundCondition2<ET>, IJoinWhere3<ET>, ET> //
        implements IJoinWhere2<ET> {

    public JoinWhere2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinWhere3<ET> nextForWhere(final EqlSentenceBuilder builder) {
        return new JoinWhere3<ET>(builder);
    }

    @Override
    protected IJoinCompoundCondition2<ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition2<ET>(builder);
    }

    @Override
    protected IJoinComparisonOperator2<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new JoinComparisonOperator2<ET>(builder);
    }

}
