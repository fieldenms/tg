package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class EntityResultQueryModel<T extends AbstractEntity> extends SingleResultQueryModel {

    public EntityResultQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens);
    }

    public EntityResultQueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class<T> resultType) {
	super(tokens, resultType);
    }
}
