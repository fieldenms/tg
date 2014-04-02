package ua.com.fielden.platform.entity.query.fluent;

abstract class AbstractQueryLink {

    private Tokens tokens;

    protected AbstractQueryLink() {
    }

    protected <T> T copy(final T parent, final Tokens tokens) {
        ((AbstractQueryLink) parent).setTokens(tokens);
        return parent;
    }

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

    public void setTokens(final Tokens tokens) {
        this.tokens = tokens;
    }
}