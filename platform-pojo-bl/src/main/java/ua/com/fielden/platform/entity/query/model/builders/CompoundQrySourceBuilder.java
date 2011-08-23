package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.model.elements.IEntQuerySource;
import ua.com.fielden.platform.entity.query.model.elements.JoinType;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder extends AbstractTokensBuilder {

    protected CompoundQrySourceBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final TokenCategory cat, final Object value) {
	super(parent, dbVersion);
	getTokens().add(new Pair<TokenCategory, Object>(cat, value));
	setChild(new QrySourceBuilder(this, dbVersion));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case ON: //eats token
	    finaliseChild();
	    setChild(new ConditionsBuilder(this, getDbVersion()));
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
	    finaliseChild();
	    //throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
//	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
//	final IEntQuerySource mainSource = (IEntQuerySource) iterator.next().getValue();
//	final List<EntQueryCompoundSourceModel> otherSources = new ArrayList<EntQueryCompoundSourceModel>();
//	for (; iterator.hasNext();) {
//	    final EntQueryCompoundSourceModel subsequentSource= (EntQueryCompoundSourceModel) iterator.next().getValue();
//	    otherSources.add(subsequentSource);
//	}
	return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new EntQueryCompoundSourceModel((IEntQuerySource) secondValue(), (JoinType) firstValue(), (ConditionsModel) thirdValue()));
    }

}
