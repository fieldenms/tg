package ua.com.fielden.platform.eql.stage3.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.queries.SubQuery3;

public class QuantifiedPredicate3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final SubQuery3 rightOperand;
    public final Quantifier quantifier;
    public final ComparisonOperator operator;

    public QuantifiedPredicate3(final ISingleOperand3 leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final SubQuery3 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.quantifier = quantifier;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return leftOperand.sql(dbVersion) + " " + operator + " " + quantifier + " " + rightOperand.sql(dbVersion);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + operator.hashCode();
        result = prime * result + quantifier.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof QuantifiedPredicate3)) {
            return false;
        }
        
        final QuantifiedPredicate3 other = (QuantifiedPredicate3) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(quantifier, other.quantifier) &&
                Objects.equals(operator, other.operator);
    }
}