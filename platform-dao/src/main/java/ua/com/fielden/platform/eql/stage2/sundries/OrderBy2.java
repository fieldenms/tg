package ua.com.fielden.platform.eql.stage2.sundries;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;

public class OrderBy2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    public final String yieldName;
    public final boolean isDesc;

    public OrderBy2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean isDesc) {
        this.operand = operand;
        this.yieldName = null;
        this.isDesc = isDesc;
    }

    public OrderBy2(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.yieldName = yieldName;
        this.isDesc = isDesc;
    }

    public TransformationResultFromStage2To3<OrderBy3> transform(final TransformationContextFromStage2To3 context, final Yields3 yields) {
        if (operand != null) {
            final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = operand.transform(context);
            return new TransformationResultFromStage2To3<>(new OrderBy3(operandTr.item, isDesc), operandTr.updatedContext);
        } else {
            return new TransformationResultFromStage2To3<>(new OrderBy3(yields.getYieldsMap().get(yieldName), isDesc), context);
        }
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

        return (isDesc == other.isDesc) && Objects.equals(operand, other.operand) && Objects.equals(yieldName, other.yieldName);
    }
}