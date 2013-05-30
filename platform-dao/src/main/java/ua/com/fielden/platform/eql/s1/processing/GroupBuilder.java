package ua.com.fielden.platform.eql.s1.processing;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.GroupBy;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand;
import ua.com.fielden.platform.utils.Pair;

public class GroupBuilder extends AbstractTokensBuilder {

    protected GroupBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 1;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_GROUP, new GroupBy(operand));
    }
}
