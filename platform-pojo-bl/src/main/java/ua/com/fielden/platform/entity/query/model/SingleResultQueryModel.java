package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;

/**
 * Models a query returning a result that can be used as a scalar value.
 */
public abstract class SingleResultQueryModel<T extends AbstractEntity<?>> extends QueryModel<T> {

    protected SingleResultQueryModel(final ListTokenSource tokens, final Class<T> resultType, final boolean yieldAll) {
        super(tokens, resultType, yieldAll);
    }
}
