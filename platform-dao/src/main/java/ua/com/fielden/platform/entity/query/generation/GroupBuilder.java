package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.GroupBy;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;

public class GroupBuilder extends AbstractTokensBuilder {

    protected GroupBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final IDates dates) {
        super(parent, queryBuilder, paramValues, dates);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 1;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
        return new Pair<>(TokenCategory.QRY_GROUP, new GroupBy(operand));
    }
}
