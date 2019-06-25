package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.operands.OperandsBasedSet3;

public class OperandsBasedSet2 implements ISetOperand2<OperandsBasedSet3> {
    private final List<ISingleOperand2> operands;

    public OperandsBasedSet2(final List<ISingleOperand2> operands) {
        this.operands = operands;
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

    @Override
    public TransformationResult transform(final TransformationContext transformationContext) {
        // TODO Auto-generated method stub
        return null;
    }
}