package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinCondition;

final class JoinAlias<ET extends AbstractEntity<?>> extends JoinOn<ET> implements IJoinAlias<ET> {

    public JoinAlias(final Tokens tokens) {
        super(tokens);
    }
    
    @Override
    public IJoinCondition<ET> as(final String alias) {
        return new JoinOn<ET>(getTokens().joinAlias(alias));
    }
}