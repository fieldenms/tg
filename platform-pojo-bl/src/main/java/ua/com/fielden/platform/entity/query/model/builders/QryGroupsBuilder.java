package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.GroupModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QryGroupsBuilder extends AbstractTokensBuilder {

    protected QryGroupsBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	super(parent, dbVersion);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (getChild() != null && getSize() == 0) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<GroupModel> groups = new ArrayList<GroupModel>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    groups.add((GroupModel) pair.getValue());
	}

	return new Pair<TokenCategory, Object>(TokenCategory.QRY_GROUPS, new GroupsModel(groups));
    }
}
