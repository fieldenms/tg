package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.GroupBy;
import ua.com.fielden.platform.entity.query.generation.elements.GroupBys;
import ua.com.fielden.platform.utils.Pair;

public class QryGroupsBuilder extends AbstractTokensBuilder {

    protected QryGroupsBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    public GroupBys getModel() {
	if (getChild() != null && getSize() == 0) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<GroupBy> groups = new ArrayList<GroupBy>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    groups.add((GroupBy) pair.getValue());
	}

	return new GroupBys(groups);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
