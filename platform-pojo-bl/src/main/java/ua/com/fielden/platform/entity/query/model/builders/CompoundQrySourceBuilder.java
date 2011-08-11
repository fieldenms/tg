package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.model.elements.JoinType;
import ua.com.fielden.platform.entity.query.model.structure.IEntQuerySource;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder extends AbstractTokensBuilder {

    protected CompoundQrySourceBuilder(final AbstractTokensBuilder parent, final TokenCategory cat, final Object value) {
	super(parent);
	getTokens().add(new Pair<TokenCategory, Object>(cat, value));
	setChild(new QrySourceBuilder(this));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case ON: //eats token
	    setChild(new ConditionsBuilder(this));
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
