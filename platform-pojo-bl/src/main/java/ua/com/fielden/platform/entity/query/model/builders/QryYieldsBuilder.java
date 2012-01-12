package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QryYieldsBuilder extends AbstractTokensBuilder {

    protected QryYieldsBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    // TODO handle yield().entity(String joinAlias) properly

    public YieldsModel getModel() {
	if (getChild() != null && getSize() == 0) {
	    finaliseChild();
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<YieldModel> yields = new ArrayList<YieldModel>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    yields.add((YieldModel) pair.getValue());
	}

	return new YieldsModel(yields);
    }


    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
