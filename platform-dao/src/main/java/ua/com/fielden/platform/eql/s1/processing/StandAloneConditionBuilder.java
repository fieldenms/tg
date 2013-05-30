package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition;
import ua.com.fielden.platform.eql.s1.elements.GroupedConditions;
import ua.com.fielden.platform.eql.s1.elements.ICondition;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneConditionBuilder extends AbstractTokensBuilder {
    private final boolean negated;

    public StandAloneConditionBuilder(final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues, final ConditionModel exprModel, final boolean negated) {
	super(null, queryBuilder, paramValues);
	this.negated = negated;
	setChild(new ConditionBuilder(this, queryBuilder, paramValues));

	for (final Pair<TokenCategory, Object> tokenPair : exprModel.getTokens()) {
	    add(tokenPair.getKey(), tokenPair.getValue());
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

    public GroupedConditions getModel() {
	if (getChild() != null) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}

	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final ICondition firstCondition = (ICondition) iterator.next().getValue();
	final List<CompoundCondition> otherConditions = new ArrayList<CompoundCondition>();
	for (; iterator.hasNext();) {
	    final CompoundCondition subsequentCompoundCondition = (CompoundCondition) iterator.next().getValue();
	    otherConditions.add(subsequentCompoundCondition);
	}
	return new GroupedConditions(negated, firstCondition, otherConditions);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}