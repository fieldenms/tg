package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.GroupModel;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.utils.Pair;

public class GroupBuilder extends AbstractTokensBuilder {

    protected GroupBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    public boolean isClosing() {
	return getSize() == 1;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_GROUP, new GroupModel(operand));
    }
}
