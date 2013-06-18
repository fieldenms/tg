package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.OrderBy1;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.YIELD;

public class OrderByBuilder1 extends AbstractTokensBuilder1 {
    private boolean descOrder;
    protected OrderByBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final QueryTokens orderDirection = (QueryTokens) secondValue();
	if (firstCat() == YIELD) {
	    return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy1((String) firstValue(), QueryTokens.ASC.equals(orderDirection) ? false : true));
	} else {
	    final ISingleOperand1 operand = getModelForSingleOperand(firstCat(), firstValue());
	    return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy1(operand, QueryTokens.ASC.equals(orderDirection) ? false : true));
	}
    }
}
