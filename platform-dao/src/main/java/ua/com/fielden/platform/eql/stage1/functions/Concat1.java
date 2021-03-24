package ua.com.fielden.platform.eql.stage1.functions;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.functions.Concat2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class Concat1 extends AbstractFunction1<Concat2> {

    private final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands;

    public Concat1(final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands) {
        this.operands = operands;
    }

    @Override
    public Concat2 transform(final TransformationContext context) {
        return new Concat2(operands.stream().map(el -> el.transform(context)).collect(toList()));
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

        if (!(obj instanceof Concat1)) {
            return false;
        }
        
        final Concat1 other = (Concat1) obj;
        
        return Objects.equals(operands, other.operands);
    }
}