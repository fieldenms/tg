package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.utils.EntityUtils;



public class NullTest extends AbstractCondition {
    private final ISingleOperand operand;
    private final boolean negated;

    @Override
    public String sql() {
	if (operand instanceof EntProp) {
	    final EntProp prop = (EntProp) operand;
	    if (EntityUtils.isUnionEntityType(prop.getPropType())) {
		return negated ? "(location__workshop" + " IS NOT NULL" + " OR location__wagonslot" + " IS NOT NULL" + ")" : //
		    "(location__workshop" + " IS NULL" + " AND location__wagonslot" + " IS NULL" + ")";
	    }
	}

	return operand.sql() + (negated ? " IS NOT NULL" : " IS NULL");
    }

    public NullTest(final ISingleOperand operand, final boolean negated) {
	this.operand = operand;
	this.negated = negated;
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
	if (!(obj instanceof NullTest)) {
	    return false;
	}
	final NullTest other = (NullTest) obj;
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
    protected List<IPropertyCollector> getCollection() {
	return new ArrayList<IPropertyCollector>(){{add(operand);}};
    }
}