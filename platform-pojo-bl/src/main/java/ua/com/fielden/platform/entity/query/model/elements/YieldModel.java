package ua.com.fielden.platform.entity.query.model.elements;



public class YieldModel {
    private final ISingleOperand operand;
    private final String alias;

    public YieldModel(final ISingleOperand operand, final String alias) {
	super();
	this.operand = operand;
	this.alias = alias;
    }

    public ISingleOperand getOperand() {
        return operand;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
	if (!(obj instanceof YieldModel)) {
	    return false;
	}
	final YieldModel other = (YieldModel) obj;
	if (alias == null) {
	    if (other.alias != null) {
		return false;
	    }
	} else if (!alias.equals(other.alias)) {
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
}