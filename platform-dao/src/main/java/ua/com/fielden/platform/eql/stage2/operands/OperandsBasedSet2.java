package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.OperandsBasedSet3;
import ua.com.fielden.platform.utils.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public record OperandsBasedSet2 (List<ISingleOperand2<? extends ISingleOperand3>> operands)
        implements ISetOperand2<OperandsBasedSet3>, ToString.IFormattable
{

    @Override
    public TransformationResultFromStage2To3<OperandsBasedSet3> transform(final TransformationContextFromStage2To3 context) {
        final List<ISingleOperand3> transformedOperands = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;
        for (final ISingleOperand2<? extends ISingleOperand3> singleOperand : operands) {
            final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = singleOperand.transform(currentContext);
            transformedOperands.add(operandTr.item);
            currentContext = operandTr.updatedContext;
        }

        return new TransformationResultFromStage2To3<>(new OperandsBasedSet3(transformedOperands), currentContext);
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
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operands", operands)
                .$();
    }

}
