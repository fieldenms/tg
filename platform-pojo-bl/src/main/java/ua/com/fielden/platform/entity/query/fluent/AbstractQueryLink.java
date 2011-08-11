package ua.com.fielden.platform.entity.query.fluent;




/**
 * Represents some arbitrary link of a query chain. Holds references to the preceding link and its operators, which are the part of the method, which produced this link.
 *
 * @author TG Team
 *
 */
abstract class AbstractQueryLink {

    private final Tokens tokens;

    protected AbstractQueryLink(final Tokens queryTokens) {
	this.tokens = queryTokens;
    }

    public Tokens getTokens() {
	return tokens;//.clon();
    }

    @Override
    public String toString() {
        return tokens.toString();
    }
}
