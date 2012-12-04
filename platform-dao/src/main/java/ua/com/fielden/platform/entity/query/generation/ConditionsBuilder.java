package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.Conditions;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.utils.Pair;

public class ConditionsBuilder extends AbstractTokensBuilder {

    protected ConditionsBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
//	setChild(new ConditionBuilder(this, queryBuilder, paramValues));
    }

    @Override
    public boolean isClosing() {
	return false;
    }

    @Override
    public boolean canBeClosed() {
	return getChild() == null;
    }

    public Conditions getModel() {
	if (getChild() != null) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	if (!iterator.hasNext()) {
	    return new Conditions(null);
	} else {
	    final ICondition firstCondition = (ICondition) iterator.next().getValue();
	    final List<CompoundCondition> otherConditions = new ArrayList<CompoundCondition>();
	    for (; iterator.hasNext();) {
		final CompoundCondition subsequentCompoundCondition = (CompoundCondition) iterator.next().getValue();
		otherConditions.add(subsequentCompoundCondition);
	    }
	    return new Conditions(firstCondition, otherConditions);
	}
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
