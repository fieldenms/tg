package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;

/**
 * Models a query returning results of type {@link EntityAggregates}.
 */
public class AggregatedResultQueryModel extends QueryModel<EntityAggregates> {

    public AggregatedResultQueryModel(final ListTokenSource tokens, final boolean yieldAll) {
        super(tokens, EntityAggregates.class, yieldAll);
    }
}
