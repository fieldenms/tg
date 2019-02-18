package ua.com.fielden.platform.eql.stage1.builders;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.YIELD;

import ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.OrderBy1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.utils.Pair;

public class OrderByBuilder extends AbstractTokensBuilder {
    private boolean descOrder;

    protected OrderByBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
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
