package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand;
import ua.com.fielden.platform.eql.s1.elements.Yield;
import ua.com.fielden.platform.utils.Pair;

public class YieldBuilder extends AbstractTokensBuilder {

    protected YieldBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	final String alias = (String) secondValue();
	final boolean requiredHint = (secondCat() == TokenCategory.AS_ALIAS_REQUIRED);
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new Yield(operand, alias == null ? "" : alias, requiredHint));
    }
}
