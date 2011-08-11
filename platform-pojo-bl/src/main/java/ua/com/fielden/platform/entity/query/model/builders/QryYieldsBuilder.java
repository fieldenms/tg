package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QryYieldsBuilder extends AbstractTokensBuilder {

    protected QryYieldsBuilder(final AbstractTokensBuilder parent) {
	super(parent);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    // handle yield().entity(String joinAlias) properly

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (getChild() != null && getSize() == 0) {
	    finaliseChild();
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<YieldModel> yields = new ArrayList<YieldModel>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    yields.add((YieldModel) pair.getValue());
	}

	return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELDS, new YieldsModel(yields));
    }
}
