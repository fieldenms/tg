package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.Yield1;
import ua.com.fielden.platform.utils.Pair;

public class YieldBuilder1 extends AbstractTokensBuilder1 {

    protected YieldBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final ISingleOperand1 operand = getModelForSingleOperand(firstCat(), firstValue());
	final String alias = (String) secondValue();
	final boolean requiredHint = (secondCat() == TokenCategory.AS_ALIAS_REQUIRED);
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new Yield1(operand, alias == null ? "" : alias, requiredHint));
    }
}
