package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.model.structure.IEntQuerySource;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class QrySourcesBuilder extends AbstractTokensBuilder {

    protected QrySourcesBuilder(final AbstractTokensBuilder parent) {
	super(parent);
	setChild(new QrySourceBuilder(this));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case INNER_JOIN: //eats token
	case LEFT_JOIN: //eats token
	    finaliseChild();
	    setChild(new CompoundQrySourceBuilder(this, cat, value));
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
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
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
