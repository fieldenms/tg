package ua.com.fielden.platform.eql.stage1.elements.operands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.OperandsBasedSet2;

public class OperandsBasedSet1 implements ISetOperand1<OperandsBasedSet2> {
    private final List<ISingleOperand1<? extends ISingleOperand2>> operands;

    public OperandsBasedSet1(final List<ISingleOperand1<? extends ISingleOperand2>> operands) {
        this.operands = operands;
    }

    @Override
    public TransformationResult<OperandsBasedSet2> transform(final PropsResolutionContext resolutionContext) {
        final List<ISingleOperand2> transformedOperands = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = resolutionContext;
        for (final ISingleOperand1<? extends ISingleOperand2> singleOperand : operands) {
            final TransformationResult<? extends ISingleOperand2> operandTransformationResult = singleOperand.transform(resolutionContext);
            transformedOperands.add(operandTransformationResult.getItem());
            currentResolutionContext = operandTransformationResult.getUpdatedContext();
        }

        return new TransformationResult<OperandsBasedSet2>(new OperandsBasedSet2(transformedOperands), currentResolutionContext);
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

        if (!(obj instanceof OperandsBasedSet1)) {
            return false;
        }
        
        final OperandsBasedSet1 other = (OperandsBasedSet1) obj;
        
        return Objects.equals(operands, other.operands);
    }
}