package ua.com.fielden.platform.eql.stage3.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISetOperand3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class SetTest3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISetOperand3 rightOperand;
    public final boolean negated;

    public SetTest3(final ISingleOperand3 leftOperand, final boolean negated, final ISetOperand3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public String sql() {
        return leftOperand.sql() + (negated ? " NOT IN " : " IN ") + rightOperand.sql();
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

        if (!(obj instanceof SetTest3)) {
            return false;
        }
        
        final SetTest3 other = (SetTest3) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(negated, other.negated);
    }
}