package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public abstract class QueryModel<T extends AbstractEntity<?>> {
    private final List<Pair<TokenCategory, Object>> tokens;
    private Class<T> resultType;

    @Override
    public String toString() {
        return tokens.toString();
    }

    public QueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	this.tokens = tokens;
    }

    public QueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class<T> resultType) {
	this(tokens);
	this.resultType = resultType;
    }

    public List<Pair<TokenCategory, Object>> getTokens() {
        return tokens;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
	result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (! (obj instanceof QueryModel)) {
	    return false;
	}
	final QueryModel<?> that = (QueryModel<?>) obj;

	if (resultType == null) {
	    if (that.resultType != null) {
		return false;
	    }
	} else if (!resultType.equals(that.resultType)) {
	    return false;
	}
	if (tokens == null) {
	    if (that.tokens != null) {
		return false;
	    }
	} else if (!tokens.equals(that.tokens)) {
	    return false;
	}
	return true;
    }

    public Class<? extends AbstractEntity<?>> getResultType() {
        return resultType;
    }
}