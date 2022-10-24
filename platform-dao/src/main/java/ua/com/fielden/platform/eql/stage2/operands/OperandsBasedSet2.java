package ua.com.fielden.platform.eql.stage2.operands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.OperandsBasedSet3;

public class OperandsBasedSet2 implements ISetOperand2<OperandsBasedSet3> {
    private final List<ISingleOperand2<? extends ISingleOperand3>> operands;

    public OperandsBasedSet2(final List<ISingleOperand2<? extends ISingleOperand3>> operands) {
        this.operands = operands;
    }

    @Override
    public TransformationResult2<OperandsBasedSet3> transform(final TransformationContext2 context) {
        final List<ISingleOperand3> transformedOperands = new ArrayList<>();
        TransformationContext2 currentContext = context;
        for (final ISingleOperand2<? extends ISingleOperand3> singleOperand : operands) {
            final TransformationResult2<? extends ISingleOperand3> operandTr = singleOperand.transform(currentContext);
            transformedOperands.add(operandTr.item);
            currentContext = operandTr.updatedContext;
        }

        return new TransformationResult2<>(new OperandsBasedSet3(transformedOperands), currentContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final ISingleOperand2<? extends ISingleOperand3> operand : operands) {
            result.addAll(operand.collectProps());
        }
        return result;
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

        if (!(obj instanceof OperandsBasedSet2)) {
            return false;
        }
        final OperandsBasedSet2 other = (OperandsBasedSet2) obj;
        
        return Objects.equals(operands, other.operands);
    }
}