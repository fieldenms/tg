package ua.com.fielden.platform.eql.s1.processing;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundSource1;
import ua.com.fielden.platform.eql.s1.elements.ISource1;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder1 extends AbstractTokensBuilder1 {

    protected CompoundQrySourceBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final TokenCategory cat, final Object value) {
        super(parent, queryBuilder);
        getTokens().add(new Pair<TokenCategory, Object>(cat, value));
        setChild(new QrySourceBuilder1(this, queryBuilder));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        switch (cat) {
        case ON: //eats token
            finaliseChild();
            final ConditionsBuilder1 onCondition = new ConditionsBuilder1(this, getQueryBuilder());
            onCondition.setChild(new ConditionBuilder1(onCondition, getQueryBuilder()));
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
            final ITokensBuilder1 last = getChild();
            setChild(null);
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new CompoundSource1((ISource1) secondValue(), (JoinType) firstValue(), ((ConditionsBuilder1) last).getModel()));
        } else {
            return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new CompoundSource1((ISource1) secondValue(), (JoinType) firstValue(), null));
        }
    }
}
