package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

abstract class AbstractFunctionBuilder extends AbstractTokensBuilder {

    protected AbstractFunctionBuilder(final AbstractTokensBuilder parent) {
	super(parent);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 1;
    }

    abstract Object getModel();

    @Override
    public Pair<TokenCategory, Object> getResult() {
	return new Pair<TokenCategory, Object>(TokenCategory.FUNCTION_MODEL, getModel());
    }

}
