package ua.com.fielden.platform.eql.stage1.builders;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.sources.CompoundSource1;
import ua.com.fielden.platform.eql.stage1.elements.sources.IQrySource1;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder extends AbstractTokensBuilder {

    protected CompoundQrySourceBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final TokenCategory cat, final Object value) {
        super(parent, queryBuilder);
        getTokens().add(new Pair<TokenCategory, Object>(cat, value));
        setChild(new QrySourceBuilder(this, queryBuilder));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        switch (cat) {
        case ON: //eats token
            finaliseChild();
            final ConditionsBuilder onCondition = new ConditionsBuilder(this, getQueryBuilder());
            onCondition.setChild(new ConditionBuilder(onCondition, getQueryBuilder()));
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
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new CompoundSource1((IQrySource1) secondValue(), (JoinType) firstValue(), ((ConditionsBuilder) last).getModel()));
        } else {
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new CompoundSource1((IQrySource1) secondValue(), (JoinType) firstValue(), null));
        }
    }
}
