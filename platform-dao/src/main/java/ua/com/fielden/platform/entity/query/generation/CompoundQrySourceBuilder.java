package ua.com.fielden.platform.entity.query.generation;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.generation.elements.IEntQuerySource;
import ua.com.fielden.platform.utils.Pair;

public class CompoundQrySourceBuilder extends AbstractTokensBuilder {

    protected CompoundQrySourceBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues, final TokenCategory cat, final Object value) {
	super(parent, dbVersion, paramValues);
	getTokens().add(new Pair<TokenCategory, Object>(cat, value));
	setChild(new QrySourceBuilder(this, dbVersion, paramValues));
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
	switch (cat) {
	case ON: //eats token
	    finaliseChild();
	    setChild(new ConditionsBuilder(this, getDbVersion(), getParamValues()));
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
	    final ITokensBuilder last = getChild();
	    setChild(null);
	    return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new EntQueryCompoundSourceModel((IEntQuerySource) secondValue(), (JoinType) firstValue(), ((ConditionsBuilder) last).getModel()));
	} else {
	    return new Pair<TokenCategory, Object>(TokenCategory.QRY_COMPOUND_SOURCE, new EntQueryCompoundSourceModel((IEntQuerySource) secondValue(), (JoinType) firstValue(), null));
	}
    }
}
