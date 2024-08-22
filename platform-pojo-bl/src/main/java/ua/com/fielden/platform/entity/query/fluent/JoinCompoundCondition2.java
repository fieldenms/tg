package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere2;

final class JoinCompoundCondition2<ET extends AbstractEntity<?>> //
        extends CompoundCondition<IJoinWhere2<ET>, IJoinCompoundCondition1<ET>> //
        implements IJoinCompoundCondition2<ET> {

    public JoinCompoundCondition2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinWhere2<ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new JoinWhere2<ET>(builder);
    }

    @Override
    protected IJoinCompoundCondition1<ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition1<ET>(builder);
    }

}
