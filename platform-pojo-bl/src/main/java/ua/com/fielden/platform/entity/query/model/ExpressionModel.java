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
    private final List<Pair<TokenCategory, Object>> tokens = new ArrayList<Pair<TokenCategory, Object>>();
    private boolean contextPrefixNeeded;

    protected ExpressionModel() {}

    public ExpressionModel(final List<Pair<TokenCategory, Object>> tokens) {
	this.tokens.addAll(tokens);
    }

    @Override
    public int hashCode() {
        return toString().hashCode() * 23;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof ExpressionModel)) {
	    return false;
	}

	return toString().equals(obj.toString());
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
}