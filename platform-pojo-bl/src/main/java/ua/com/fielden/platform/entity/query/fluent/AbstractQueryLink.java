package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

abstract class AbstractQueryLink {

	private final Tokens tokens;
	
    protected AbstractQueryLink(final Tokens tokens) {
        if (tokens == null) {
            throw new EqlException("Invalid argument -- tokens should not be null.");
        }

        this.tokens = tokens; // TODO may need to clone...
    }

	public Tokens getTokens() {
		return tokens;
	}

	@Override
	public String toString() {
		return getTokens().toString();
	}

	@Override
	public int hashCode() {
		return 31 * tokens.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final AbstractQueryLink that = (AbstractQueryLink) obj;
		if (tokens == null && that.tokens != null || !tokens.equals(that.tokens)) {
		    return false;
		}
		
		return true;
	}
}