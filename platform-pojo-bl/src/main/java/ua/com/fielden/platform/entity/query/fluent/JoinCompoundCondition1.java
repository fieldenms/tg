package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere1;

final class JoinCompoundCondition1<ET extends AbstractEntity<?>> //
        extends CompoundCondition<IJoinWhere1<ET>, IJoinCompoundCondition0<ET>> //
        implements IJoinCompoundCondition1<ET> {

    public JoinCompoundCondition1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinWhere1<ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new JoinWhere1<ET>(builder);
    }

    @Override
    protected IJoinCompoundCondition0<ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition0<ET>(builder);
    }

}
