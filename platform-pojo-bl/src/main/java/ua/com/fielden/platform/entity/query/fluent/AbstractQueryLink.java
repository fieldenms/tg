package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

abstract class AbstractQueryLink {

	private Tokens tokens;

	static protected <T> T copy(final T next, final Tokens tokens) {
        if (tokens == null) {
        	throw new EqlException("Invalid argument -- tokens should not be null.");
        }
        
		((AbstractQueryLink) next).setTokens(tokens);
		return next;
	}

	public Tokens getTokens() {
        if (tokens == null) {
        	throw new EqlException("Invalid situation. Tokens have not been assigned yet!");
        }
		return tokens;
	}

	@Override
	public String toString() {
		return tokens.toString();
	}

	private void setTokens(final Tokens tokens) {
        if (this.tokens == null) {
        	this.tokens = tokens;
        } else {
        	throw new EqlException("Invalid situation. Should not replace already assigned tokens!");
        }
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractQueryLink other = (AbstractQueryLink) obj;
		if (tokens == null) {
			if (other.tokens != null)
				return false;
		} else if (!tokens.equals(other.tokens))
			return false;
		return true;
	}
}