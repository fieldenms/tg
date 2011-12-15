package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.model.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.model.elements.IEntQuerySource;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QrySourcesBuilder extends AbstractTokensBuilder {

    protected QrySourcesBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
	setChild(new QrySourceBuilder(this, dbVersion, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case INNER_JOIN: //eats token
	case LEFT_JOIN: //eats token
	    finaliseChild();
	    setChild(new CompoundQrySourceBuilder(this, getDbVersion(), getParamValues(), cat, value));
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

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (getChild() != null) {
	    finaliseChild();
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final IEntQuerySource mainSource = (IEntQuerySource) iterator.next().getValue();
	final List<EntQueryCompoundSourceModel> otherSources = new ArrayList<EntQueryCompoundSourceModel>();
	for (; iterator.hasNext();) {
	    final EntQueryCompoundSourceModel subsequentSource= (EntQueryCompoundSourceModel) iterator.next().getValue();
	    otherSources.add(subsequentSource);
	}
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_SOURCES, new EntQuerySourcesModel(mainSource, otherSources));
    }
}