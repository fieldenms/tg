package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.functions.Concat3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class Concat2 extends AbstractFunction2<Concat3> {

    private final List<ISingleOperand2<? extends ISingleOperand3>> operands;

    public Concat2(final List<ISingleOperand2<? extends ISingleOperand3>> operands) {
        this.operands = operands;
    }

    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public TransformationResult<Concat3> transform(final TransformationContext context) {
        final List<ISingleOperand3> transformed = new ArrayList<>();
        TransformationContext currentContext = context;
        for (final ISingleOperand2<? extends ISingleOperand3> operand : operands) {
            final TransformationResult<? extends ISingleOperand3> operandTr = operand.transform(currentContext);
            transformed.add(operandTr.item);
            currentContext = operandTr.updatedContext;
        }
        return new TransformationResult<Concat3>(new Concat3(transformed), currentContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        final Set<EntProp2> result = new HashSet<>();
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
    
        if (!(obj instanceof Concat2)) {
            return false;
        }
        
        final Concat2 other = (Concat2) obj;
        
        return Objects.equals(operands, other.operands);
    }
}