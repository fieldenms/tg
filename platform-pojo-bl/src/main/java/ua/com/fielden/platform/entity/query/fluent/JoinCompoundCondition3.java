package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere3;

final class JoinCompoundCondition3<ET extends AbstractEntity<?>> //
        extends CompoundCondition<IJoinWhere3<ET>, IJoinCompoundCondition2<ET>> //
        implements IJoinCompoundCondition3<ET> {

    public JoinCompoundCondition3(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IJoinWhere3<ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new JoinWhere3<ET>(builder);
    }

    @Override
    protected IJoinCompoundCondition2<ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new JoinCompoundCondition2<ET>(builder);
    }

}
