package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.Yield1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.utils.Pair;

public class YieldBuilder extends AbstractTokensBuilder {

    protected YieldBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
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
