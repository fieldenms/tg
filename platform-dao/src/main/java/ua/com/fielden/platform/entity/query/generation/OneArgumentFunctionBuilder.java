package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

abstract class OneArgumentFunctionBuilder extends AbstractTokensBuilder {

    protected OneArgumentFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 1;
    }

    abstract Object getModel();

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<>(TokenCategory.FUNCTION_MODEL, getModel());
    }

}
