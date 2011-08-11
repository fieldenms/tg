package ua.com.fielden.platform.equery;

/**
 * Represents some arbitrary link of a query chain. Holds references to the preceding link and its operators, which are the part of the method, which produced this link.
 * 
 * @author nc
 * 
 */
abstract class AbstractQueryLink {

    final QueryTokens tokens;

    protected AbstractQueryLink(final QueryTokens queryTokens) {
	this.tokens = queryTokens;
    }

    public QueryTokens getTokens() {
	return tokens.clon();
    }
}
