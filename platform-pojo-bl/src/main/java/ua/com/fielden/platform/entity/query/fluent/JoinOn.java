package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinWhere0;

class JoinOn<ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements IJoinCondition<ET> {

    public JoinOn(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IJoinWhere0<ET> on() {
        return new JoinWhere0<ET>(builder.on());
    }

}
