package ua.com.fielden.platform.entity.query.fluent;

abstract class AbstractQueryLink {

	private Tokens tokens;

	static protected <T> T copy(final T next, final Tokens tokens) {
		((AbstractQueryLink) next).setTokens(tokens);
		return next;
	}

	public Tokens getTokens() {
		return tokens;
	}

	@Override
	public String toString() {
		return tokens.toString();
	}

	private void setTokens(final Tokens tokens) {
		this.tokens = tokens;
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