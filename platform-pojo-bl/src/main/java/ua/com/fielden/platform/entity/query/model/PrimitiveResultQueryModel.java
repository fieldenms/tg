package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class PrimitiveResultQueryModel extends SingleResultQueryModel {

    public PrimitiveResultQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens);
    }

    public PrimitiveResultQueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class<?> entityType) {
	super(tokens, entityType);
    }
}
