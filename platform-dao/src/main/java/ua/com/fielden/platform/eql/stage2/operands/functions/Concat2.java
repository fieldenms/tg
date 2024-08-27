package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.Concat3;
import ua.com.fielden.platform.utils.ToString;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;

public class Concat2 extends AbstractFunction2<Concat3> {

    private final List<ISingleOperand2<? extends ISingleOperand3>> operands;

    public Concat2(final List<ISingleOperand2<? extends ISingleOperand3>> operands) {
        super(STRING_PROP_TYPE);
        this.operands = operands;
    }

    @Override
    public TransformationResultFromStage2To3<Concat3> transform(final TransformationContextFromStage2To3 context) {
        final List<ISingleOperand3> transformed = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;
        for (final ISingleOperand2<? extends ISingleOperand3> operand : operands) {
            final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = operand.transform(currentContext);
            transformed.add(operandTr.item);
            currentContext = operandTr.updatedContext;
        }
        return new TransformationResultFromStage2To3<>(new Concat3(transformed, type), currentContext);
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
        return this == obj
               || obj instanceof Concat2 that
                  && Objects.equals(operands, that.operands);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("operands", operands);
    }

}
