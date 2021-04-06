package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSource;
import ua.com.fielden.platform.entity.query.generation.elements.ISource;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder extends AbstractTokensBuilder {

    protected CompoundQrySourceBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues, final TokenCategory cat, final Object value) {
        super(parent, queryBuilder, paramValues);
        getTokens().add(new Pair<>(cat, value));
        setChild(new QrySourceBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        switch (cat) {
        case ON: //eats token
            finaliseChild();
            final ConditionsBuilder onCondition = new ConditionsBuilder(this, getQueryBuilder(), getParamValues());
            onCondition.setChild(new ConditionBuilder(onCondition, getQueryBuilder(), getParamValues()));
            setChild(onCondition);
            break;
        default:
            super.add(cat, value);
            break;
        }
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    @Override
    public boolean canBeClosed() {
        return getChild().canBeClosed();
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        if (getChild() != null) {
            final ITokensBuilder last = getChild();
            setChild(null);
            return new Pair<>(TokenCategory.QRY_COMPOUND_SOURCE, new CompoundSource((ISource) secondValue(), (JoinType) firstValue(), ((ConditionsBuilder) last).getModel()));
        } else {
            return new Pair<>(TokenCategory.QRY_COMPOUND_SOURCE, new CompoundSource((ISource) secondValue(), (JoinType) firstValue(), null));
        }
    }
}
