package ua.com.fielden.platform.eql.stage2.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class LikeTest2 extends AbstractCondition2 {
    public final ISingleOperand2 leftOperand;
    public final ISingleOperand2 rightOperand;
    public final boolean negated;
    public final boolean caseInsensitive;

    public LikeTest2(final ISingleOperand2 leftOperand, final ISingleOperand2 rightOperand, final boolean negated, final boolean caseInsensitive) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore() || rightOperand.ignore();
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
        
        if (!(obj instanceof LikeTest2)) {
            return false;
        }
        
        final LikeTest2 other = (LikeTest2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(caseInsensitive, other.caseInsensitive) &&
                Objects.equals(negated, other.negated);
    }
}