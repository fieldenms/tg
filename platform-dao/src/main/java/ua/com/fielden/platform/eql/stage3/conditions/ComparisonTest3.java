package ua.com.fielden.platform.eql.stage3.conditions;

import static java.lang.String.format;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class ComparisonTest3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISingleOperand3 rightOperand;
    public final ComparisonOperator operator;

    public ComparisonTest3(final ISingleOperand3 leftOperand, final ComparisonOperator operator, final ISingleOperand3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return format("%s %s %s", leftOperand.sql(dbVersion), operator, rightOperand.sql(dbVersion));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + operator.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ComparisonTest3)) {
            return false;
        }
        
        final ComparisonTest3 other = (ComparisonTest3) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(operator, other.operator);
   }
}