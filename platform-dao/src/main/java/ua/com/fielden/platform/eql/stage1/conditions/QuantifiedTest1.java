package ua.com.fielden.platform.eql.stage1.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.Quantifier;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.SubQuery1;
import ua.com.fielden.platform.eql.stage2.conditions.QuantifiedTest2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class QuantifiedTest1 implements ICondition1<QuantifiedTest2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand;
    private final SubQuery1 rightOperand;
    private final Quantifier quantifier;
    private final ComparisonOperator operator;

    public QuantifiedTest1(final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final SubQuery1 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.quantifier = quantifier;
    }

    @Override
    public QuantifiedTest2 transform(final TransformationContext context) {
        return new QuantifiedTest2(leftOperand.transform(context), operator, quantifier, rightOperand.transform(context));
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

        if (!(obj instanceof QuantifiedTest1)) {
            return false;
        }
        
        final QuantifiedTest1 other = (QuantifiedTest1) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(quantifier, other.quantifier) &&
                Objects.equals(operator, other.operator);
    }
}