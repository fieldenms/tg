package ua.com.fielden.platform.eql.stage3.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class LikeTest3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISingleOperand3 rightOperand;
    public final boolean negated;
    public final boolean caseInsensitive;

    public LikeTest3(final ISingleOperand3 leftOperand, final ISingleOperand3 rightOperand, final boolean negated, final boolean caseInsensitive) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public String sql() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseInsensitive ? 1231 : 1237);
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
        
        if (!(obj instanceof LikeTest3)) {
            return false;
        }
        
        final LikeTest3 other = (LikeTest3) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(caseInsensitive, other.caseInsensitive) &&
                Objects.equals(negated, other.negated);
    }
}