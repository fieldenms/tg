package ua.com.fielden.platform.eql.stage0.functions;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.FUNCTION_MODEL;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage0.AbstractTokensBuilder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.utils.Pair;

abstract class TwoArgumentsFunctionBuilder extends AbstractTokensBuilder {

    protected TwoArgumentsFunctionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
    }

    @Override
    public boolean isClosing() {
        return getSize() == 2;
    }

    abstract Object getModel();

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<TokenCategory, Object>(FUNCTION_MODEL, getModel());
    }

}
