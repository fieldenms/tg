package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.Yield;
import ua.com.fielden.platform.eql.s1.elements.Yields;
import ua.com.fielden.platform.utils.Pair;

public class QryYieldsBuilder extends AbstractTokensBuilder {

    protected QryYieldsBuilder(final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(null, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    // TODO handle yield().entity(String joinAlias) properly

    public Yields getModel() {
	if (getChild() != null && getSize() == 0) {
	    finaliseChild();
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
	}

	final Yields result = new Yields();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    final Yield yield = (Yield) pair.getValue();

	    result.addYield(yield);
	}

	return result;
    }


    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
