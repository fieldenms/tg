package ua.com.fielden.platform.eql.stage1.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ComparisonTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class ComparisonTest1 implements ICondition1<ComparisonTest2> {
    private final ISingleOperand1<? extends ISingleOperand2> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2> rightOperand;
    private final ComparisonOperator operator;

    public ComparisonTest1(final ISingleOperand1<? extends ISingleOperand2> leftOperand, final ComparisonOperator operator, final ISingleOperand1<? extends ISingleOperand2> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    @Override
    public TransformationResult<ComparisonTest2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> leftOperandTransformationResult = leftOperand.transform(resolutionContext);
        final TransformationResult<? extends ISingleOperand2> rightOperandTransformationResult = rightOperand.transform(leftOperandTransformationResult.getUpdatedContext());
        return new TransformationResult<ComparisonTest2>(new ComparisonTest2(leftOperandTransformationResult.getItem(), operator, rightOperandTransformationResult.getItem()), rightOperandTransformationResult.getUpdatedContext());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof ComparisonTest1)) {
            return false;
        }
        
        final ComparisonTest1 other = (ComparisonTest1) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(operator, other.operator);
    }
}