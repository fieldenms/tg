package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;


public class LikeTest implements ICondition {
    private final ISingleOperand leftOperand;
    private final ISingleOperand rightOperand;
    private final boolean negated;
    private final boolean caseInsensitive;

    @Override
    public String sql() {
	return leftOperand.sql() + (negated ? " NOT LIKE " : " LIKE ") + rightOperand.sql();
    }

    public LikeTest(final ISingleOperand leftOperand, final ISingleOperand rightOperand, final boolean negated, final boolean caseInsensitive) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
	this.caseInsensitive = caseInsensitive;
    }

    @Override
    public List<EntProp> getLocalProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.addAll(leftOperand.getLocalProps());
	result.addAll(rightOperand.getLocalProps());
	return result;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(leftOperand.getLocalSubQueries());
	result.addAll(rightOperand.getLocalSubQueries());
	return result;
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	result.addAll(leftOperand.getAllValues());
	result.addAll(rightOperand.getAllValues());
	return result;
    }

    @Override
    public boolean ignore() {
	return leftOperand.ignore() || rightOperand.ignore();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (caseInsensitive ? 1231 : 1237);
	result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
	result = prime * result + (negated ? 1231 : 1237);
	result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
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
	if (!(obj instanceof LikeTest)) {
	    return false;
	}
	final LikeTest other = (LikeTest) obj;
	if (caseInsensitive != other.caseInsensitive) {
	    return false;
	}
	if (leftOperand == null) {
	    if (other.leftOperand != null) {
		return false;
	    }
	} else if (!leftOperand.equals(other.leftOperand)) {
	    return false;
	}
	if (negated != other.negated) {
	    return false;
	}
	if (rightOperand == null) {
	    if (other.rightOperand != null) {
		return false;
	    }
	} else if (!rightOperand.equals(other.rightOperand)) {
	    return false;
	}
	return true;
    }
}