package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class Conditions extends AbstractCondition {
    private final ICondition firstCondition;
    private final List<CompoundCondition> otherConditions;

    public String sql() {
        final StringBuffer sb = new StringBuffer();
        sb.append(firstCondition.sql());
        for (final CompoundCondition compound : otherConditions) {
            sb.append(" " + compound.sql());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(firstCondition);
        for (final CompoundCondition compound : otherConditions) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public Conditions(final ICondition firstCondition, final List<CompoundCondition> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions = otherConditions;
    }

    public Conditions(final ICondition firstCondition) {
	this.firstCondition = firstCondition;
	this.otherConditions = Collections.emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	if (firstCondition != null) {
	    result.addAll(getFirstCondition().getLocalSubQueries());
	}

	for (final CompoundCondition compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getLocalSubQueries());
	}
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	if (firstCondition != null) {
		result.addAll(getFirstCondition().getLocalProps());
	}

	for (final CompoundCondition compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getLocalProps());
	}
	return result;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	if (firstCondition != null) {
	    result.addAll(getFirstCondition().getAllValues());
	}

	for (final CompoundCondition compCondModel : getOtherConditions()) {
	    result.addAll(compCondModel.getCondition().getAllValues());
	}
	return result;
    }

    public ICondition getFirstCondition() {
        return firstCondition;
    }

    public List<CompoundCondition> getOtherConditions() {
        return otherConditions;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((firstCondition == null) ? 0 : firstCondition.hashCode());
	result = prime * result + ((otherConditions == null) ? 0 : otherConditions.hashCode());
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
	if (!(obj instanceof Conditions)) {
	    return false;
	}
	final Conditions other = (Conditions) obj;
	if (firstCondition == null) {
	    if (other.firstCondition != null) {
		return false;
	    }
	} else if (!firstCondition.equals(other.firstCondition)) {
	    return false;
	}
	if (otherConditions == null) {
	    if (other.otherConditions != null) {
		return false;
	    }
	} else if (!otherConditions.equals(other.otherConditions)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	if (!firstCondition.ignore()) {
	    return false;
	}
	
	for (CompoundCondition compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		return false;
	    }
	}
	
	return true;
    }
}