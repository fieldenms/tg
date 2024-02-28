package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class AggregatedResultQueryModel extends QueryModel<EntityAggregates> {

    protected AggregatedResultQueryModel() {
    }

    public AggregatedResultQueryModel(final List<? extends Token> tokens, final boolean yieldAll) {
        super(tokens, EntityAggregates.class, yieldAll);
    }
}
