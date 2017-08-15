package ua.com.fielden.platform.entity.query.fluent;

abstract class AbstractQueryLink {

    private Tokens tokens;

    static protected <T> T copy(final T parent, final Tokens tokens) {
        if (tokens == null) {
        	throw new RuntimeException("NO TOKENS WHILE SET!");
        }
    	((AbstractQueryLink) parent).setTokens(tokens);
    	return parent;        
    }

    public Tokens getTokens() {
        if (tokens == null) {
        	throw new RuntimeException("NO TOKENS WHILE GET!");
        }
        return tokens;
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    void setTokens(final Tokens tokens) {
        if (this.tokens == null) {
        	this.tokens = tokens;
        } else {
        	throw new RuntimeException("TRYING TO REPLACE ALREADY ASSIGNED TOKENS!");
        }
    	
    }
}