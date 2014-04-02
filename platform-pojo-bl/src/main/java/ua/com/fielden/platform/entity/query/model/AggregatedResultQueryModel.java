package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class AggregatedResultQueryModel extends QueryModel<EntityAggregates> {

    protected AggregatedResultQueryModel() {
    }

    public AggregatedResultQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
        super(tokens, EntityAggregates.class);
    }
}