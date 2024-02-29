package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;

public class AggregatedResultQueryModel extends QueryModel<EntityAggregates> {

    public AggregatedResultQueryModel(final ListTokenSource tokens, final boolean yieldAll) {
        super(tokens, EntityAggregates.class, yieldAll);
    }
}
