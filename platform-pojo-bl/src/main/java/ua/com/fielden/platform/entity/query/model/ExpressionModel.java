package ua.com.fielden.platform.entity.query.model;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents a computational model for expressions, which can be used together with entity query API.
 *
 * @author TG Team
 *
 */
public class ExpressionModel {
    private final List<Pair<TokenCategory, Object>> tokens = new ArrayList<>();
    private boolean contextPrefixNeeded = true;

    protected ExpressionModel() {}

    public ExpressionModel(final List<Pair<TokenCategory, Object>> tokens) {
	this.tokens.addAll(tokens);
    }

    public List<Pair<TokenCategory, Object>> getTokens() {
        return tokens;
    }

    @Override
    public String toString() {
	return tokens.toString();
    }

    public boolean isContextPrefixNeeded() {
        return contextPrefixNeeded;
    }

    public void setContextPrefixNeeded(final boolean contextPrefixNeeded) {
        this.contextPrefixNeeded = contextPrefixNeeded;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (contextPrefixNeeded ? 1231 : 1237);
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
	if (!(obj instanceof ExpressionModel)) {
	    return false;
	}
	final ExpressionModel other = (ExpressionModel) obj;
	if (contextPrefixNeeded != other.contextPrefixNeeded) {
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
}