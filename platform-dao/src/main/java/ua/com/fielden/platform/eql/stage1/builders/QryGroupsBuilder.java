package ua.com.fielden.platform.eql.stage1.builders;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.GroupBy1;
import ua.com.fielden.platform.eql.stage1.elements.GroupBys1;
import ua.com.fielden.platform.utils.Pair;

public class QryGroupsBuilder extends AbstractTokensBuilder {

    protected QryGroupsBuilder(final EntQueryGenerator queryBuilder) {
        super(null, queryBuilder);
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
