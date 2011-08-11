package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class UnorderedQueryModel extends QueryModel {

    public UnorderedQueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens);
    }

    public UnorderedQueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class resultType) {
	super(tokens, resultType);
    }
}
