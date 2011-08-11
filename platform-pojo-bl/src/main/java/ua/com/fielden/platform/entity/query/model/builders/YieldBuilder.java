package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.structure.ISingleOperand;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class YieldBuilder extends AbstractTokensBuilder {

    protected YieldBuilder(final AbstractTokensBuilder parent) {
	super(parent);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	final String alias = getSize() == 2 ? (String) secondValue() : "id";
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new YieldModel(operand, alias));
    }
}
