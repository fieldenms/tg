package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.GroupBy1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.utils.Pair;

public class GroupBuilder1 extends AbstractTokensBuilder1 {

    protected GroupBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
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
