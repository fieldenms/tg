package ua.com.fielden.platform.entity.query.model.elements;


public class GroupModel {
    private final ISingleOperand operand;

    public GroupModel(final ISingleOperand operand) {
	super();
	this.operand = operand;
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
	if (!(obj instanceof GroupModel)) {
	    return false;
	}
	final GroupModel other = (GroupModel) obj;
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
