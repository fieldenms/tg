package ua.com.fielden.platform.eql.stage1.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.meta.Quantifier;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.SubQuery1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.QuantifiedTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SubQuery2;

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
    public TransformationResult<QuantifiedTest2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<?>> leftOperandTr = leftOperand.transform(context);
        final TransformationResult<SubQuery2> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);
        
        return new TransformationResult<QuantifiedTest2>(new QuantifiedTest2(leftOperandTr.item, operator, quantifier, rightOperandTr.item), rightOperandTr.updatedContext);
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