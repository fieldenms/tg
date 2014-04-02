package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.utils.Pair;

public class CompoundConditionBuilder1 extends AbstractTokensBuilder1 {

    protected CompoundConditionBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final TokenCategory cat, final Object value) {
        super(parent, queryBuilder);
        getTokens().add(new Pair<TokenCategory, Object>(cat, value));
        setChild(new ConditionBuilder1(this, queryBuilder));
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<TokenCategory, Object>(TokenCategory.COMPOUND_CONDITION, new CompoundCondition1((LogicalOperator) getTokens().get(0).getValue(), (ICondition1) getTokens().get(1).getValue()));
    }

}
