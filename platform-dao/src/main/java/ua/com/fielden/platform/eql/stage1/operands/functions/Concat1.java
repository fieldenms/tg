package ua.com.fielden.platform.eql.stage1.operands.functions;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.Concat2;

public class Concat1 extends AbstractFunction1<Concat2> {

    private final List<? extends ISingleOperand1<? extends ISingleOperand2<?>>> operands;

    public Concat1(final List<? extends ISingleOperand1<? extends ISingleOperand2<?>>> operands) {
        this.operands = operands;
    }

    @Override
    public Concat2 transform(final TransformationContextFromStage1To2 context) {
        return new Concat2(operands.stream().map(el -> el.transform(context)).collect(toList()));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operands.stream().map(el -> el.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
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
