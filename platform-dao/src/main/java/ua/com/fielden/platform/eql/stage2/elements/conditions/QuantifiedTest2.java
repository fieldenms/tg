package ua.com.fielden.platform.eql.stage2.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.Quantifier;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.QuantifiedTest3;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntQuery3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class QuantifiedTest2 extends AbstractCondition2<QuantifiedTest3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final EntQuery2 rightOperand;
    public final Quantifier quantifier;
    public final ComparisonOperator operator;

    public QuantifiedTest2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final EntQuery2 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.quantifier = quantifier;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public TransformationResult<QuantifiedTest3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> leftOperandTransformationResult = leftOperand.transform(context);
        final TransformationResult<EntQuery3> rightOperandTransformationResult = rightOperand.transform(leftOperandTransformationResult.updatedContext);
        
        return new TransformationResult<QuantifiedTest3>(new QuantifiedTest3(leftOperandTransformationResult.item, operator, quantifier, rightOperandTransformationResult.item), rightOperandTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + ((quantifier == null) ? 0 : quantifier.hashCode());
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof QuantifiedTest2)) {
            return false;
        }
        
        final QuantifiedTest2 other = (QuantifiedTest2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(quantifier, other.quantifier) &&
                Objects.equals(operator, other.operator);
    }
}