package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.Yield1;
import ua.com.fielden.platform.eql.stage1.elements.Yields1;
import ua.com.fielden.platform.utils.Pair;

public class QryYieldsBuilder extends AbstractTokensBuilder {

    protected QryYieldsBuilder(final EntQueryGenerator queryBuilder) {
        super(null, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    // TODO handle yield().entity(String joinAlias) properly

    public Yields1 getModel() {
        if (getChild() != null && getSize() == 0) {
            finaliseChild();
            //throw new RuntimeException("Unable to produce result - unfinished model state!");
        }

        final Yields1 result = new Yields1();
        for (final Pair<TokenCategory, Object> pair : getTokens()) {
            final Yield1 yield = (Yield1) pair.getValue();

            result.addYield(yield);
        }

        return result;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new RuntimeException("Not applicable!");
    }
}
