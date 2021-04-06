package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.utils.Pair;

public class CompoundConditionBuilder extends AbstractTokensBuilder {

    protected CompoundConditionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final TokenCategory cat, final Object value) {
        super(parent, queryBuilder, paramValues);
        getTokens().add(new Pair<>(cat, value));
        setChild(new ConditionBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<>(TokenCategory.COMPOUND_CONDITION, new CompoundCondition((LogicalOperator) getTokens().get(0).getValue(), (ICondition) getTokens().get(1).getValue()));
    }

}
