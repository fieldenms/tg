package ua.com.fielden.platform.eql.s2.elements;

public class NullTest2 extends AbstractCondition2 {
    private final ISingleOperand2 operand;
    private final boolean negated;

    public NullTest2(final ISingleOperand2 operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    @Override
    public String toString() {
        return (negated ? " NOT " : " ") + operand;
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
            System.out.println(" nullTest: 1");
            return false;
        }
        if (!(obj instanceof NullTest2)) {
            System.out.println(" nullTest: 2");
            return false;
        }
        final NullTest2 other = (NullTest2) obj;
        if (negated != other.negated) {
            System.out.println(" nullTest: 3");
            return false;
        }
        if (operand == null) {
            if (other.operand != null) {
                System.out.println(" nullTest: 4");
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            System.out.println(" nullTest: 5");
            return false;
        }
        return true;
    }
}