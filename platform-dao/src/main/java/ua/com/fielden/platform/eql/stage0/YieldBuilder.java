package ua.com.fielden.platform.eql.stage0;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.etc.Yield1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class YieldBuilder extends AbstractTokensBuilder {

    public static String ABSENT_ALIAS = ""; // Used for the cases where yield requires no alias (sub-query with single yield).
    
    protected YieldBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final ISingleOperand1<? extends ISingleOperand2<?>> operand = getModelForSingleOperand(firstCat(), firstValue());
        final String alias = (String) secondValue();
        final boolean nonnullableHint = (secondCat() == TokenCategory.AS_ALIAS_REQUIRED);
        return new Pair<TokenCategory, Object>(TokenCategory.QRY_YIELD, new Yield1(operand, alias == null ? ABSENT_ALIAS : alias, nonnullableHint));
    }
}
