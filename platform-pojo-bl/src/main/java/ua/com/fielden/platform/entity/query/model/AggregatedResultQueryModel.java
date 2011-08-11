package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class AggregatedResultQueryModel extends UnorderedQueryModel {

    public AggregatedResultQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens, EntityAggregates.class);
    }

}
