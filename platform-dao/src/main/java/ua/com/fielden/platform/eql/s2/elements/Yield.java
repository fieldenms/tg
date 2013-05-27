package ua.com.fielden.platform.eql.s2.elements;


public class Yield {
    private final ISingleOperand2 operand;
    private final String alias;
    private final boolean requiredHint;
    private ResultQueryYieldDetails info;


    public Yield(final ISingleOperand2 operand, final String alias, final boolean requiredHint) {
	this.operand = operand;
	this.alias = alias;
	this.requiredHint = requiredHint;
    }

    public Yield(final ISingleOperand2 operand, final String alias) {
	this(operand, alias, false);
    }

    @Override
    public String toString() {
	return alias;//sql();
    }

    public ISingleOperand2 getOperand() {
	return operand;
    }

    public String getAlias() {
	return alias;
    }

    public boolean isCompositePropertyHeader() {
	return info != null && info.isCompositeProperty();
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
	if (!(obj instanceof Yield)) {
	    return false;
	}
	final Yield other = (Yield) obj;
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

    public ResultQueryYieldDetails getInfo() {
	return info;
    }

    public void setInfo(final ResultQueryYieldDetails info) {
	this.info = info;
    }

    protected boolean isRequiredHint() {
        return requiredHint;
    }
}