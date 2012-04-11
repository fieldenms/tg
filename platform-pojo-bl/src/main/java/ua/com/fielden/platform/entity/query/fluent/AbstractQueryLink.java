package ua.com.fielden.platform.entity.query.fluent;

abstract class AbstractQueryLink {

    private final Tokens tokens;

    protected AbstractQueryLink(final Tokens tokens) {
	this.tokens = tokens;
    }

    public Tokens getTokens() {
	return tokens;
    }

    @Override
    public String toString() {
        return tokens.toString();
    }
}