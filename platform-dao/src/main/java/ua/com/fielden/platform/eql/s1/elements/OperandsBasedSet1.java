package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.s2.elements.OperandsBasedSet2;


public class OperandsBasedSet1 implements ISetOperand1<OperandsBasedSet2>{
    private final List<ISingleOperand1<? extends ISingleOperand2>> operands;

    public OperandsBasedSet1(final List<ISingleOperand1<? extends ISingleOperand2>> operands) {
	super();
	this.operands = operands;
    }

    @Override
    public OperandsBasedSet2 transform(final TransformatorToS2 resolver) {
	final List<ISingleOperand2> transformedOperands = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> singleOperand : operands) {
	    transformedOperands.add(singleOperand.transform(resolver));
	}

	return new OperandsBasedSet2(transformedOperands);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((operands == null) ? 0 : operands.hashCode());
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
	if (!(obj instanceof OperandsBasedSet1)) {
	    return false;
	}
	final OperandsBasedSet1 other = (OperandsBasedSet1) obj;
	if (operands == null) {
	    if (other.operands != null) {
		return false;
	    }
	} else if (!operands.equals(other.operands)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}