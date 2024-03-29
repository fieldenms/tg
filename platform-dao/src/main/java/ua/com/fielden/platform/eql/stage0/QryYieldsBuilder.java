package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.eql.stage1.sundries.Yields1.EMPTY_YIELDS;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.exceptions.EqlStage0ProcessingException;
import ua.com.fielden.platform.eql.stage1.sundries.Yield1;
import ua.com.fielden.platform.eql.stage1.sundries.Yields1;
import ua.com.fielden.platform.utils.Pair;

public class QryYieldsBuilder extends AbstractTokensBuilder {

    protected QryYieldsBuilder(final QueryModelToStage1Transformer queryBuilder) {
        super(null, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    // TODO handle yield().entity(String joinAlias) properly

    public Yields1 getModel() {
        if (getChild() != null && getTokens().isEmpty()) {
            finaliseChild();
            //throw new EqlStage0ProcessingException("Unable to produce result - unfinished model state!");
        }

        if (getTokens().isEmpty()) {
            return EMPTY_YIELDS;
        }

        final List<Yield1> yields= new ArrayList<>();
        for (final Pair<TokenCategory, Object> pair : getTokens()) {
            yields.add((Yield1) pair.getValue());
        }

        return new Yields1(yields);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new EqlStage0ProcessingException("Not applicable!");
    }
}
