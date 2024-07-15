package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.eql.stage1.sundries.GroupBys1.EMPTY_GROUP_BYS;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.exceptions.EqlStage0ProcessingException;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBy1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBys1;
import ua.com.fielden.platform.utils.Pair;

public class QryGroupsBuilder extends AbstractTokensBuilder {

    protected QryGroupsBuilder(final QueryModelToStage1Transformer queryBuilder) {
        super(null, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    public GroupBys1 getModel() {
        if (getChild() != null && getTokens().isEmpty()) {
            throw new EqlStage0ProcessingException("Unable to produce result - unfinished model state!");
        }

        if (getTokens().isEmpty()) {
            return EMPTY_GROUP_BYS;
        }

        final List<GroupBy1> groups = new ArrayList<>();
        for (final Pair<TokenCategory, Object> pair : getTokens()) {
            groups.add((GroupBy1) pair.getValue());
        }

        return new GroupBys1(groups);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new EqlStage0ProcessingException("Not applicable!");
    }
}
