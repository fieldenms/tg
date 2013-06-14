package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.utils.Pair;

public class ConditionsBuilder1 extends AbstractTokensBuilder1 {

    protected ConditionsBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
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

    public Conditions1 getModel() {
	if (getChild() != null) {
	    throw new RuntimeException("Unable to produce result - unfinished model state!");
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	if (!iterator.hasNext()) {
	    return new Conditions1();
	} else {
	    final ICondition1<? extends ICondition2> firstCondition = (ICondition1<? extends ICondition2>) iterator.next().getValue();
	    final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
	    for (; iterator.hasNext();) {
		final CompoundCondition1 subsequentCompoundCondition = (CompoundCondition1) iterator.next().getValue();
		otherConditions.add(subsequentCompoundCondition);
	    }
	    return new Conditions1(firstCondition, otherConditions);
	}
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	throw new RuntimeException("Not applicable!");
    }
}
