package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;

final class JoinCompoundCondition0<ET extends AbstractEntity<?>> //
        extends Join<ET> //
        implements IJoinCompoundCondition0<ET> {

    public JoinCompoundCondition0(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IJoinWhere0<ET> and() {
        return new JoinWhere0<ET>(builder.and());
    }

    @Override
    public IJoinWhere0<ET> or() {
        return new JoinWhere0<ET>(builder.or());
    }

}
