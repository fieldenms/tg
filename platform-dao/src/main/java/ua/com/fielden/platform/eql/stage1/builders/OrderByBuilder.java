package ua.com.fielden.platform.eql.stage1.builders;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.YIELD;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.OrderBy1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
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
            throw new EqlStage1ProcessingException("Not yet.");
        } else {
            final ISingleOperand1<? extends ISingleOperand2<?>> operand = getModelForSingleOperand(firstCat(), firstValue());
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new OrderBy1(operand, QueryTokens.ASC.equals(orderDirection) ? false : true));
        }
    }
}
