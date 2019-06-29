package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.OrderBy3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class OrderBy2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    public final boolean isDesc;

    public OrderBy2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean isDesc) {
        this.operand = operand;
        this.isDesc = isDesc;
    }

    public TransformationResult<OrderBy3> transform(final TransformationContext context) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        return new TransformationResult<OrderBy3>(new OrderBy3(operandTransformationResult.item, isDesc), operandTransformationResult.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderBy2)) {
            return false;
        }
        
        final OrderBy2 other = (OrderBy2) obj;

        return Objects.equals(isDesc, other.isDesc) &&
                Objects.equals(operand, other.operand);
    }
}