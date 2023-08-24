package ua.com.fielden.platform.eql.stage2.operands.functions;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.Concat3;

public class Concat2 extends AbstractFunction2<Concat3> {

    private final List<ISingleOperand2<? extends ISingleOperand3>> operands;

    public Concat2(final List<ISingleOperand2<? extends ISingleOperand3>> operands) {
        super(STRING_PROP_TYPE);
        this.operands = operands;
    }

    @Override
    public TransformationResult2<Concat3> transform(final TransformationContext2 context) {
        final List<ISingleOperand3> transformed = new ArrayList<>();
        TransformationContext2 currentContext = context;
        for (final ISingleOperand2<? extends ISingleOperand3> operand : operands) {
            final TransformationResult2<? extends ISingleOperand3> operandTr = operand.transform(currentContext);
            transformed.add(operandTr.item);
            currentContext = operandTr.updatedContext;
        }
        return new TransformationResult2<>(new Concat3(transformed, type), currentContext);
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
    
        if (!(obj instanceof Concat2)) {
            return false;
        }
        
        final Concat2 other = (Concat2) obj;
        
        return Objects.equals(operands, other.operands);
    }
}