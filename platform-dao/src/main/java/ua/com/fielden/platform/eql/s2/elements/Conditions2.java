package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

public class Conditions2 extends AbstractCondition2 {
    private final List<List<ICondition2>> allConditions;
    private final boolean negated;

    public Conditions2(final boolean negated, final List<List<ICondition2>> allConditions) {
	this.allConditions = allConditions;
	this.negated = negated;
    }

    @Override
    protected List<IElement2> getCollection() {
	final List<IElement2> result = new ArrayList<IElement2>();

	for (final List<ICondition2> compoundCondition : allConditions) {
	    for (final ICondition2 iCondition1 : compoundCondition) {
		result.add(iCondition1);
	    }
	}
	return result;
    }

    @Override
    public boolean ignore() {
	return allConditions.size() == 0;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((allConditions == null) ? 0 : allConditions.hashCode());
	result = prime * result + (negated ? 1231 : 1237);
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof Conditions2)) {
	    return false;
	}
	final Conditions2 other = (Conditions2) obj;
	if (allConditions == null) {
	    if (other.allConditions != null) {
		return false;
	    }
	} else if (!allConditions.equals(other.allConditions)) {
	    System.out.println("Conditions 4: " + allConditions + " vs " + other.allConditions);
	    return false;
	}
	if (negated != other.negated) {
	    return false;
	}
	return true;
    }
}