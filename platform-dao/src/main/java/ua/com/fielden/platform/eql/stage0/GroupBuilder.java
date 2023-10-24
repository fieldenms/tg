package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_GROUP;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.sundries.GroupBy1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;

public class GroupBuilder extends AbstractTokensBuilder {

    protected GroupBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 1;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final ISingleOperand1<? extends ISingleOperand2<?>> operand = getModelForSingleOperand(firstCat(), firstValue());
        return new Pair<TokenCategory, Object>(QRY_GROUP, new GroupBy1(operand));
    }
}
