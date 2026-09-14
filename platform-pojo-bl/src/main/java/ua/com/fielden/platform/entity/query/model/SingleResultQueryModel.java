package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.List;

/**
 * Models a query returning a result that can be used as a scalar value.
 */
public abstract class SingleResultQueryModel<T extends AbstractEntity<?>> extends QueryModel<T> {

    protected SingleResultQueryModel(final List<? extends Token> tokens, final Class<T> resultType, final boolean yieldAll) {
        super(tokens, resultType, yieldAll);
    }
}
