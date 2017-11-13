package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.GroupBy1;
import ua.com.fielden.platform.eql.stage1.elements.ISingleOperand1;
import ua.com.fielden.platform.utils.Pair;

public class GroupBuilder extends AbstractTokensBuilder {

    protected GroupBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 1;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final ISingleOperand1 operand = getModelForSingleOperand(firstCat(), firstValue());
        return new Pair<TokenCategory, Object>(TokenCategory.QRY_GROUP, new GroupBy1(operand));
    }
}
