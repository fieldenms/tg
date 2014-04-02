package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCondition;

class JoinAlias<ET extends AbstractEntity<?>> extends JoinOn<ET> implements IJoinAlias<ET> {

    JoinAlias(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public IJoinCondition<ET> as(final String alias) {
        return new JoinOn<ET>(getTokens().joinAlias(alias));
    }
}