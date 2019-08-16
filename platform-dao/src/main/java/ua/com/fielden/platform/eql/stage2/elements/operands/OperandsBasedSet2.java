package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.elements.operands.OperandsBasedSet3;

public class OperandsBasedSet2 implements ISetOperand2<OperandsBasedSet3> {
    private final List<ISingleOperand2<? extends ISingleOperand3>> operands;

    public OperandsBasedSet2(final List<ISingleOperand2<? extends ISingleOperand3>> operands) {
        this.operands = operands;
    }

    @Override
    public TransformationResult<OperandsBasedSet3> transform(final TransformationContext context) {
        final List<ISingleOperand3> transformedOperands = new ArrayList<>();
        TransformationContext currentContext = context;
        for (final ISingleOperand2<? extends ISingleOperand3> singleOperand : operands) {
            final TransformationResult<? extends ISingleOperand3> operandTransformationResult = singleOperand.transform(context);
            transformedOperands.add(operandTransformationResult.item);
            currentContext = operandTransformationResult.updatedContext;
        }

        return new TransformationResult<OperandsBasedSet3>(new OperandsBasedSet3(transformedOperands), currentContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operands == null) ? 0 : operands.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OperandsBasedSet2)) {
            return false;
        }
        final OperandsBasedSet2 other = (OperandsBasedSet2) obj;
        
        return Objects.equals(operands, other.operands);
    }
}