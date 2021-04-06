package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.utils.Pair;

public class YieldBuilder extends AbstractTokensBuilder {

    protected YieldBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
        final String alias = (String) secondValue();
        final boolean requiredHint = (secondCat() == TokenCategory.AS_ALIAS_REQUIRED);
        return new Pair<>(TokenCategory.QRY_YIELD, new Yield(operand, alias == null ? "" : alias, requiredHint));
    }
}
