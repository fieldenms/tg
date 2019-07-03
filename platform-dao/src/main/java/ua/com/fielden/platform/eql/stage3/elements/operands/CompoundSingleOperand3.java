package ua.com.fielden.platform.eql.stage3.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;

public class CompoundSingleOperand3 {
    public final ISingleOperand3 operand;
    public final ArithmeticalOperator operator;

    public CompoundSingleOperand3(final ISingleOperand3 operand, final ArithmeticalOperator operator) {
        this.operand = operand;
        this.operator = operator;
    }

    public String sql(final DbVersion dbVersion) {
        return operator.getValue() + operand.sql(dbVersion);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CompoundSingleOperand3)) {
            return false;
        }
        
        final CompoundSingleOperand3 other = (CompoundSingleOperand3) obj;
        
        return Objects.equals(operand, other.operand) &&
                Objects.equals(operator, other.operator);
    }
}
