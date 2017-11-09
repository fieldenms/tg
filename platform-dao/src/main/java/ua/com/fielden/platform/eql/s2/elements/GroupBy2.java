package ua.com.fielden.platform.eql.s2.elements;

public class GroupBy2 {
    private final ISingleOperand2 operand;

    public GroupBy2(final ISingleOperand2 operand) {
        super();
        this.operand = operand;
    }

    public ISingleOperand2 getOperand() {
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
        if (!(obj instanceof GroupBy2)) {
            return false;
        }
        final GroupBy2 other = (GroupBy2) obj;
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
