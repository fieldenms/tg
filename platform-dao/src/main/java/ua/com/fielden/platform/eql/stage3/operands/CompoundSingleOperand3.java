package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

public class CompoundSingleOperand3 {
    public final ISingleOperand3 operand;
    public final ArithmeticalOperator operator;

    public CompoundSingleOperand3(final ISingleOperand3 operand, final ArithmeticalOperator operator) {
        this.operand = operand;
        this.operator = operator;
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return operator.value + operand.sql(metadata, dbVersion);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand.hashCode();
        result = prime * result + operator.hashCode();
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

        return Objects.equals(operand, other.operand) && Objects.equals(operator, other.operator);
    }

}
