package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QueryModel {
    private final List<Pair<TokenCategory, Object>> tokens;
    private Class resultType;

    @Override
    public String toString() {
        return tokens.toString();
    }

    public QueryModel(final List<Pair<TokenCategory, Object>> tokens) {
	this.tokens = tokens;
    }

    public QueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class resultType) {
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
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof QueryModel)) {
	    return false;
	}
	final QueryModel other = (QueryModel) obj;

	if (resultType == null) {
	    if (other.resultType != null) {
		return false;
	    }
	} else if (!resultType.equals(other.resultType)) {
	    return false;
	}
	if (tokens == null) {
	    if (other.tokens != null) {
		return false;
	    }
	} else if (!tokens.equals(other.tokens)) {
	    return false;
	}
	return true;
    }

    public Class getResultType() {
        return resultType;
    }
}