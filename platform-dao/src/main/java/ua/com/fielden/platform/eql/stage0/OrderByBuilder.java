package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.DESC;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_YIELD;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.YIELD;

import ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.etc.OrderBy1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class OrderByBuilder extends AbstractTokensBuilder {

    protected OrderByBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
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
            return new Pair<>(QRY_YIELD, new OrderBy1((String) firstValue(), orderDirection == DESC));
        } else {
            final ISingleOperand1<? extends ISingleOperand2<?>> operand = getModelForSingleOperand(firstCat(), firstValue());
            return new Pair<>(QRY_YIELD, new OrderBy1(operand, orderDirection == DESC));
        }
    }
}
