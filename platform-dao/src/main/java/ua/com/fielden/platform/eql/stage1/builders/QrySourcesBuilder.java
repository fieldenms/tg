package ua.com.fielden.platform.eql.stage1.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.eql.stage1.elements.sources.CompoundSource1;
import ua.com.fielden.platform.eql.stage1.elements.sources.IQrySource1;
import ua.com.fielden.platform.eql.stage1.elements.sources.Sources1;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.utils.Pair;

public class QrySourcesBuilder extends AbstractTokensBuilder {

    protected QrySourcesBuilder(final EntQueryGenerator queryBuilder) {
        super(/* parent = */ null, queryBuilder);
        setChild(new QrySourceBuilder(this, queryBuilder));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        switch (cat) {
        case JOIN_TYPE: //eats token
            finaliseChild();
            setChild(new CompoundQrySourceBuilder(this, getQueryBuilder(), cat, value));
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
        return getChild() == null;
    }

    public Sources1 getModel() {
        if (getChild() != null) {
            finaliseChild();
        }
        final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
        final IQrySource1<? extends IQrySource2> mainSource = (IQrySource1<? extends IQrySource2>) iterator.next().getValue();
        final List<CompoundSource1> otherSources = new ArrayList<CompoundSource1>();
        for (; iterator.hasNext();) {
            final CompoundSource1 subsequentSource = (CompoundSource1) iterator.next().getValue();
            otherSources.add(subsequentSource);
        }
        return new Sources1(mainSource, otherSources);

    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        throw new EqlStage1ProcessingException("Result cannot be obtained here. Use getModel() to obtain the final result.");
    }
}