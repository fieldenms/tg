package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand;
import ua.com.fielden.platform.eql.s1.elements.OrderBy;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.YIELD;

public class OrderByBuilder extends AbstractTokensBuilder {
    private boolean descOrder;
    protected OrderByBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final QueryTokens orderDirection = (QueryTokens) secondValue();
	if (firstCat() == YIELD) {
	    return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy((String) firstValue(), QueryTokens.ASC.equals(orderDirection) ? false : true));
	} else {
	    final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	    return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy(operand, QueryTokens.ASC.equals(orderDirection) ? false : true));
	}
    }
}
