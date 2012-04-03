package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public abstract class SingleResultQueryModel<T extends AbstractEntity<?>> extends QueryModel<T> {

    protected SingleResultQueryModel() {

    }

    protected SingleResultQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens);
    }

    protected SingleResultQueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class<T> resultType) {
	super(tokens, resultType);
    }
}
