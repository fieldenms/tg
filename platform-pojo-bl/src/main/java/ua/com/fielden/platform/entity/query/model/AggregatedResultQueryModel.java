package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.EntityAggregates;

import java.util.List;

/**
 * Models a query returning results of type {@link EntityAggregates}.
 */
public class AggregatedResultQueryModel extends QueryModel<EntityAggregates> {

    public AggregatedResultQueryModel(final List<? extends Token> tokens, final boolean yieldAll) {
        super(tokens, EntityAggregates.class, yieldAll);
    }
}
