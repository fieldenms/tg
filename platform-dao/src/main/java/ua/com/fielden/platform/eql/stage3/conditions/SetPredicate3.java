package ua.com.fielden.platform.eql.stage3.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISetOperand3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class SetPredicate3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISetOperand3 rightOperand;
    public final boolean negated;

    public SetPredicate3(final ISingleOperand3 leftOperand, final boolean negated, final ISetOperand3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return leftOperand.sql(dbVersion) + (negated ? " NOT IN " : " IN ") + rightOperand.sql(dbVersion);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SetPredicate3)) {
            return false;
        }
        
        final SetPredicate3 other = (SetPredicate3) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                (negated == other.negated);
    }
}