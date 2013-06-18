package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.utils.Pair;

public class GroupedConditionsBuilder1 extends AbstractTokensBuilder1 {

    private final boolean negated;

    protected GroupedConditionsBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final boolean negated) {
	super(parent, queryBuilder);
	this.negated = negated;
	setChild(new ConditionBuilder1(this, queryBuilder));
    }

    @Override
    public boolean isClosing() {
	return getLastCat().equals(TokenCategory.END_COND);
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (TokenCategory.END_COND.equals(getLastCat())) {
	    getTokens().remove(getSize() - 1);
	}
	final Iterator<Pair<TokenCategory, Object>> iterator = getTokens().iterator();
	final ICondition1<? extends ICondition2> firstCondition = (ICondition1<? extends ICondition2>) iterator.next().getValue();
	final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
	for (; iterator.hasNext();) {
	    final CompoundCondition1 subsequentCompoundCondition = (CompoundCondition1) iterator.next().getValue();
	    otherConditions.add(subsequentCompoundCondition);
	}
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, new Conditions1(negated, firstCondition, otherConditions));
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (negated ? 1231 : 1237);
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof GroupedConditionsBuilder1))
	    return false;
	final GroupedConditionsBuilder1 other = (GroupedConditionsBuilder1) obj;
	if (negated != other.negated)
	    return false;
	return true;
    }
}