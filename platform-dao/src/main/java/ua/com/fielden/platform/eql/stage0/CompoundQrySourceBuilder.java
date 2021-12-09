package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_COMPOUND_SOURCE;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.sources.ISources1;
import ua.com.fielden.platform.eql.stage1.sources.MultipleNodesSources1;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder extends AbstractTokensBuilder {
    private final ISources1<? extends ISources2<?>> leftSource;
    private final JoinType joinType;
    protected CompoundQrySourceBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final ISources1<? extends ISources2<?>> leftSource, final JoinType joinType) {
        super(parent, queryBuilder);
        this.leftSource = leftSource;
        this.joinType = joinType;
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
    public Pair<TokenCategory, Object> getResult() {
        final ITokensBuilder last = getChild();
        setChild(null);
        return new Pair<TokenCategory, Object>(QRY_COMPOUND_SOURCE, new MultipleNodesSources1(leftSource, (ISources1<? extends ISources2<?>>) firstValue(), joinType, ((ConditionsBuilder) last).getModel()));
    }
}
