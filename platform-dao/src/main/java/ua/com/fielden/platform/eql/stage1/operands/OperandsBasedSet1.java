package ua.com.fielden.platform.eql.stage1.operands;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.OperandsBasedSet2;

public class OperandsBasedSet1 implements ISetOperand1<OperandsBasedSet2> {
    private final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands;

    public OperandsBasedSet1(final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands) {
        this.operands = operands;
    }

    @Override
    public OperandsBasedSet2 transform(final TransformationContext context) {
       return new OperandsBasedSet2(operands.stream().map(el -> el.transform(context)).collect(toList()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operands.hashCode();
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