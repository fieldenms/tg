package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.model.elements.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.structure.ICondition;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class CompoundConditionBuilder extends AbstractTokensBuilder {

    protected CompoundConditionBuilder(final AbstractTokensBuilder parent, final TokenCategory cat, final Object value) {
	super(parent);
	getTokens().add(new Pair<TokenCategory, Object>(cat, value));
	setChild(new ConditionBuilder(this));
    }

    @Override
    public boolean isClosing() {
	return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	return new Pair<TokenCategory, Object>(TokenCategory.COMPOUND_CONDITION, new CompoundConditionModel((LogicalOperator) getTokens().get(0).getValue(), (ICondition) getTokens().get(1).getValue()));
    }

}
