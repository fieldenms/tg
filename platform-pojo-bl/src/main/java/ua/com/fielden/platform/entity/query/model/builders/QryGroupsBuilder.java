package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.model.elements.GroupModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QryGroupsBuilder extends AbstractTokensBuilder {

    protected QryGroupsBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    public GroupsModel getModel() {
	if (getChild() != null && getSize() == 0) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<GroupModel> groups = new ArrayList<GroupModel>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    groups.add((GroupModel) pair.getValue());
	}

	return new GroupsModel(groups);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
