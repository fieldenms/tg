package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.model.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.ICondition;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class ConditionsBuilder extends AbstractTokensBuilder {

    protected ConditionsBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	super(parent, dbVersion);
	setChild(new ConditionBuilder(this, dbVersion));
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
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final ICondition firstCondition = (ICondition) iterator.next().getValue();
	final List<CompoundConditionModel> otherConditions = new ArrayList<CompoundConditionModel>();
	for (; iterator.hasNext();) {
	    final CompoundConditionModel subsequentCompoundCondition = (CompoundConditionModel) iterator.next().getValue();
	    otherConditions.add(subsequentCompoundCondition);
	}
	return new Pair<TokenCategory, Object>(TokenCategory.CONDITIONS, new ConditionsModel(firstCondition, otherConditions));
    }
}
