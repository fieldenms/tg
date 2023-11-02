package ua.com.fielden.platform.eql.stage0;

import java.util.Iterator;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.exceptions.EqlStage0ProcessingException;
import ua.com.fielden.platform.eql.stage1.sources.IJoinNode1;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.utils.Pair;

public class QrySourcesBuilder extends AbstractTokensBuilder {

    protected QrySourcesBuilder(final QueryModelToStage1Transformer queryBuilder) {
        super(/* parent = */ null, queryBuilder);
        setChild(new QrySourceBuilder(this, queryBuilder));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        switch (cat) {
        case JOIN_TYPE: //eats token
            //finaliseChild();
            setChild(new CompoundQrySourceBuilder(this, getQueryBuilder(), obtainChild(), (JoinType) value));
            break;
        default:
            super.add(cat, value);
            break;
        }
    }

    public IJoinNode1<? extends IJoinNode2<?>> obtainChild() {
        if (getChild() != null) {
            final ITokensBuilder last = getChild();
            setChild(null);
            final Pair<TokenCategory, Object> result = last.getResult();
            return (IJoinNode1<? extends IJoinNode2<?>>) result.getValue();
        }
        return null;
    }

    @Override
    public boolean isClosing() {
        return false;
    }

    public IJoinNode1<? extends IJoinNode2<?>> getModel() {
        if (getChild() != null) {
            finaliseChild();
        }
        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        return (IJoinNode1<? extends IJoinNode2<?>>) iterator.next().getValue();
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new EqlStage0ProcessingException("Result cannot be obtained here. Use getModel() to obtain the final result.");
    }
}