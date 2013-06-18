package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

abstract class OneArgumentFunctionBuilder1 extends AbstractTokensBuilder1 {

    protected OneArgumentFunctionBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
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
