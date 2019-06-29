package ua.com.fielden.platform.eql.stage1.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class OrderBy1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final boolean isDesc;

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean isDesc) {
        this.operand = operand;
        this.isDesc = isDesc;
    }

    public OrderBy1(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.isDesc = isDesc;
    }

    public TransformationResult<OrderBy2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<?>> operandTransformationResult = operand.transform(context);
        return new TransformationResult<OrderBy2>(new OrderBy2(operandTransformationResult.item, isDesc), operandTransformationResult.updatedContext);
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

        if (!(obj instanceof OrderBy1)) {
            return false;
        }
        
        final OrderBy1 other = (OrderBy1) obj;

        return Objects.equals(isDesc, other.isDesc) &&
                Objects.equals(operand, other.operand);
    }
}