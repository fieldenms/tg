package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import ua.com.fielden.platform.utils.Pair;

public class QryYieldsBuilder extends AbstractTokensBuilder {

    protected QryYieldsBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
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
	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    yields.put(((Yield) pair.getValue()).getAlias(), (Yield) pair.getValue());
	}

	return new Yields(yields);
    }


    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
