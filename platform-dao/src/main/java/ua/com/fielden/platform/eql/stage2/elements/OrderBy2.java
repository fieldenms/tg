package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.OrderBy3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class OrderBy2 {
    public final ISingleOperand2 operand;
    public final String yieldName;
    public Yield2 yield;
    public final boolean isDesc;

    public OrderBy2(final ISingleOperand2 operand, final boolean isDesc) {
        this.operand = operand;
        this.yieldName = null;
        this.isDesc = isDesc;
    }

    public OrderBy2(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.yieldName = yieldName;
        this.isDesc = isDesc;
    }

    public Yield2 getYield() {
        return yield;
    }

    public void setYield(final Yield2 yield) {
        this.yield = yield;
    }
    
    public TransformationResult<OrderBy3> transform(final TransformationContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<OrderBy3>(new OrderBy3(operandTransformationResult.getItem(), isDesc), operandTransformationResult.getUpdatedContext());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((yieldName == null) ? 0 : yieldName.hashCode());
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
                Objects.equals(operand, other.operand) &&
                Objects.equals(yield, other.yield) &&
                Objects.equals(yieldName, other.yieldName);
    }
}