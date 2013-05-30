package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundSource;
import ua.com.fielden.platform.eql.s1.elements.ISource;
import ua.com.fielden.platform.eql.s1.elements.Sources;
import ua.com.fielden.platform.utils.Pair;

public class QrySourcesBuilder extends AbstractTokensBuilder {

    protected QrySourcesBuilder(final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(null, queryBuilder, paramValues);
	setChild(new QrySourceBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case JOIN_TYPE: //eats token
	    finaliseChild();
	    setChild(new CompoundQrySourceBuilder(this, getQueryBuilder(), getParamValues(), cat, value));
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

    public Sources getModel() {
	if (getChild() != null) {
	    finaliseChild();
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final ISource mainSource = (ISource) iterator.next().getValue();
	final List<CompoundSource> otherSources = new ArrayList<CompoundSource>();
	for (; iterator.hasNext();) {
	    final CompoundSource subsequentSource= (CompoundSource) iterator.next().getValue();
	    otherSources.add(subsequentSource);
	}
	return new Sources(mainSource, otherSources);

    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}