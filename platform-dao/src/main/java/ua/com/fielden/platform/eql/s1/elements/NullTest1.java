package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.NullTest2;



public class NullTest1 extends AbstractCondition1<NullTest2> {
    private final ISingleOperand1<ISingleOperand2> operand;
    private final boolean negated;

    public NullTest1(final ISingleOperand1<ISingleOperand2> operand, final boolean negated) {
	this.operand = operand;
	this.negated = negated;
    }

    @Override
    public NullTest2 transform(final TransformatorToS2 resolver) {
	return new NullTest2(operand.transform(resolver), negated);
    }

    @Override
    public boolean ignore() {
	return operand.ignore();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (negated ? 1231 : 1237);
	result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
	if (!(obj instanceof NullTest1)) {
	    return false;
	}
	final NullTest1 other = (NullTest1) obj;
	if (negated != other.negated) {
	    return false;
	}
	if (operand == null) {
	    if (other.operand != null) {
		return false;
	    }
	} else if (!operand.equals(other.operand)) {
	    return false;
	}
	return true;
    }

    @Override
    protected List<IElement1> getCollection() {
	return new ArrayList<IElement1>(){{add(operand);}};
    }
}