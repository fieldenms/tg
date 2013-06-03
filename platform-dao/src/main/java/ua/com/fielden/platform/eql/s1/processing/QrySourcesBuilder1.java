package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundSource1;
import ua.com.fielden.platform.eql.s1.elements.ISource1;
import ua.com.fielden.platform.eql.s1.elements.Sources1;
import ua.com.fielden.platform.utils.Pair;

public class QrySourcesBuilder1 extends AbstractTokensBuilder1 {

    protected QrySourcesBuilder1(final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(null, queryBuilder, paramValues);
	setChild(new QrySourceBuilder1(this, queryBuilder, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case JOIN_TYPE: //eats token
	    finaliseChild();
	    setChild(new CompoundQrySourceBuilder1(this, getQueryBuilder(), getParamValues(), cat, value));
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
	final ISource1 mainSource = (ISource1) iterator.next().getValue();
	final List<CompoundSource1> otherSources = new ArrayList<CompoundSource1>();
	for (; iterator.hasNext();) {
	    final CompoundSource1 subsequentSource= (CompoundSource1) iterator.next().getValue();
	    otherSources.add(subsequentSource);
	}
	return new Sources1(mainSource, otherSources);

    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}