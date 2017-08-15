package ua.com.fielden.platform.entity.query.fluent;

abstract class AbstractQueryLink {

	private Tokens tokens;

	static protected <T> T copy(final T parent, final Tokens tokens) {
		((AbstractQueryLink) parent).setTokens(tokens);
		return parent;
	}

	public Tokens getTokens() {
		return tokens;
	}

	@Override
	public String toString() {
		return tokens.toString();
	}

	void setTokens(final Tokens tokens) {
		this.tokens = tokens;
	}
}