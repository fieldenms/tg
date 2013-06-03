package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.GroupBy1;
import ua.com.fielden.platform.eql.s1.elements.GroupBys1;
import ua.com.fielden.platform.utils.Pair;

public class QryGroupsBuilder1 extends AbstractTokensBuilder1 {

    protected QryGroupsBuilder1(final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(null, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    public GroupBys1 getModel() {
	if (getChild() != null && getSize() == 0) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final List<GroupBy1> groups = new ArrayList<GroupBy1>();
	for (final Pair<TokenCategory, Object> pair : getTokens()) {
	    groups.add((GroupBy1) pair.getValue());
	}

	return new GroupBys1(groups);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
