package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ICondition1;
import ua.com.fielden.platform.utils.Pair;

public class CompoundConditionBuilder extends AbstractTokensBuilder {

    protected CompoundConditionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final TokenCategory cat, final Object value) {
        super(parent, queryBuilder);
        getTokens().add(new Pair<TokenCategory, Object>(cat, value));
        setChild(new ConditionBuilder(this, queryBuilder));
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
