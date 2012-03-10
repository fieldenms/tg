package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.OrderBy;
import ua.com.fielden.platform.utils.Pair;

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
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	final QueryTokens orderDirection = (QueryTokens) secondValue();
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy(operand, QueryTokens.ASC.equals(orderDirection) ? false : true));
    }
}
