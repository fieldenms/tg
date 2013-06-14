package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

public class Conditions2 extends AbstractCondition2 {
    private final List<List<ICondition2>> allConditions;

    public Conditions2(final List<List<ICondition2>> allConditions) {
	this.allConditions = allConditions;
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

    //    @Override
    //    public String toString() {
    //        final StringBuffer sb = new StringBuffer();
    //        sb.append(firstCondition);
    //        for (final CompoundCondition2 compound : otherConditions) {
    //            sb.append(" " + compound);
    //        }
    //        return sb.toString();
    //    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((allConditions == null) ? 0 : allConditions.hashCode());
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
	    return false;
	}
	return true;
    }

}