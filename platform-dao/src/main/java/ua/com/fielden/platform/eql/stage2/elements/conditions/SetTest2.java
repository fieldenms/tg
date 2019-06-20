package ua.com.fielden.platform.eql.stage2.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class SetTest2 extends AbstractCondition2 {
    public final ISingleOperand2 leftOperand;
    public final ISetOperand2 rightOperand;
    public final boolean negated;

    public SetTest2(final ISingleOperand2 leftOperand, final boolean negated, final ISetOperand2 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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

        if (!(obj instanceof SetTest2)) {
            return false;
        }
        
        final SetTest2 other = (SetTest2) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(negated, other.negated);
    }
}