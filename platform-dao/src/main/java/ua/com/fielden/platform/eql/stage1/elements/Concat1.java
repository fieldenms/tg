package ua.com.fielden.platform.eql.stage1.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.Concat2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class Concat1 extends AbstractFunction1<Concat2> {

    private final List<ISingleOperand1<? extends ISingleOperand2>> operands;

    public Concat1(final List<ISingleOperand1<? extends ISingleOperand2>> operands) {
        this.operands = operands;
    }

    @Override
    public TransformationResult<Concat2> transform(final PropsResolutionContext resolutionContext) {
        final List<ISingleOperand2> transformed = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = resolutionContext;
        for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
            final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
            transformed.add(operandTransformationResult.getItem());
            currentResolutionContext = operandTransformationResult.getUpdatedContext();
        }
        return new TransformationResult<Concat2>(new Concat2(transformed), currentResolutionContext);
    }

    public List<ISingleOperand1<? extends ISingleOperand2>> getOperands() {
        return operands;
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Concat1)) {
            return false;
        }
        final Concat1 other = (Concat1) obj;
        if (operands == null) {
            if (other.operands != null) {
                return false;
            }
        } else if (!operands.equals(other.operands)) {
            return false;
        }
        return true;
    }
}