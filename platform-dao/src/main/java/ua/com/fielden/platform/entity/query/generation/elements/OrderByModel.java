package ua.com.fielden.platform.entity.query.generation.elements;


public class OrderByModel {
    private final ISingleOperand operand;
    private final boolean desc;

    public String sql() {
	return operand.sql() + (desc ? " DESC" : " ASC");
    }


    @Override
    public String toString() {
	return operand + (desc ? " DESC" : " ASC");
    }

    public OrderByModel(final ISingleOperand operand, final boolean desc) {
	super();
	this.operand = operand;
	this.desc = desc;
    }

    public ISingleOperand getOperand() {
        return operand;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
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
	if (!(obj instanceof OrderByModel)) {
	    return false;
	}
	final OrderByModel other = (OrderByModel) obj;
	if (operand == null) {
	    if (other.operand != null) {
		return false;
	    }
	} else if (!operand.equals(other.operand)) {
	    return false;
	}
	return true;
    }
}