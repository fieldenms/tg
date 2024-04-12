package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;

final class JoinComparisonOperator0<ET extends AbstractEntity<?>> //
        extends ComparisonOperator<IJoinCompoundCondition0<ET>, ET> //
        implements IJoinComparisonOperator0<ET> {

    public JoinComparisonOperator0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinCompoundCondition0<ET> nextForComparisonOperator(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition0<ET>(builder);
    }

}
