package ua.com.fielden.platform.eql.stage1.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.OrderBy2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class OrderBy1 {
    public final ISingleOperand1<? extends ISingleOperand2> operand;
    public final String yieldName;
    private Yield1 yield;
    public final boolean isDesc;

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2> operand, final boolean isDesc) {
        this.operand = operand;
        this.yieldName = null;
        this.isDesc = isDesc;
    }

    public OrderBy1(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.yieldName = yieldName;
        this.isDesc = isDesc;
    }

    public Yield1 getYield() {
        return yield;
    }

    public void setYield(final Yield1 yield) {
        this.yield = yield;
    }
    
    public TransformationResult<OrderBy2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<OrderBy2>(new OrderBy2(operandTransformationResult.getItem(), isDesc), operandTransformationResult.getUpdatedContext());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((yieldName == null) ? 0 : yieldName.hashCode());
        result = prime * result + ((yield == null) ? 0 : yield.hashCode());
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
                Objects.equals(operand, other.operand) &&
                Objects.equals(yield, other.yield) &&
                Objects.equals(yieldName, other.yieldName);
    }
}