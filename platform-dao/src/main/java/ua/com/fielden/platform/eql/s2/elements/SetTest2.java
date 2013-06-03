package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;



public class SetTest2 extends AbstractCondition2 {
    private final ISingleOperand2 leftOperand;
    private final ISetOperand2 rightOperand;
    private final boolean negated;

    public SetTest2(final ISingleOperand2 leftOperand, final boolean negated, final ISetOperand2 rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.negated = negated;
    }

    @Override
    public List<EntQuery2> getLocalSubQueries() {
	final List<EntQuery2> result = new ArrayList<EntQuery2>();
	result.addAll(leftOperand.getLocalSubQueries());
	result.addAll(rightOperand.getLocalSubQueries());
	return result;    }

    @Override
    public List<EntProp2> getLocalProps() {
	final List<EntProp2> result = new ArrayList<EntProp2>();
	result.addAll(leftOperand.getLocalProps());
	result.addAll(rightOperand.getLocalProps());
	return result;
    }

    @Override
    public List<EntValue2> getAllValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	result.addAll(leftOperand.getAllValues());
	result.addAll(rightOperand.getAllValues());
	return result;
    }

    @Override
    public boolean ignore() {
	return leftOperand.ignore();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
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
	if (!(obj instanceof SetTest2)) {
	    return false;
	}
	final SetTest2 other = (SetTest2) obj;
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

    @Override
    protected List<IElement2> getCollection() {
	return new ArrayList<IElement2>(){{add(leftOperand); add(rightOperand);}};
    }
}