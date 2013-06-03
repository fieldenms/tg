package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.GroupedConditions1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.utils.Pair;

public class StandAloneConditionBuilder1 extends AbstractTokensBuilder1 {
    private final boolean negated;

    public StandAloneConditionBuilder1(final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues, final ConditionModel exprModel, final boolean negated) {
	super(null, queryBuilder, paramValues);
	this.negated = negated;
	setChild(new ConditionBuilder1(this, queryBuilder, paramValues));

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

    public GroupedConditions1 getModel() {
	if (getChild() != null) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}

	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final ICondition1 firstCondition = (ICondition1) iterator.next().getValue();
	final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
	for (; iterator.hasNext();) {
	    final CompoundCondition1 subsequentCompoundCondition = (CompoundCondition1) iterator.next().getValue();
	    otherConditions.add(subsequentCompoundCondition);
	}
	return new GroupedConditions1(negated, firstCondition, otherConditions);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}