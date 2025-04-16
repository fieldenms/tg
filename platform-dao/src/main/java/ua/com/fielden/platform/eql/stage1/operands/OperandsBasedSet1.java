package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.OperandsBasedSet2;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public record OperandsBasedSet1 (List<? extends ISingleOperand1<? extends ISingleOperand2<?>>> operands)
        implements ISetOperand1<OperandsBasedSet2>, ToString.IFormattable
{

    @Override
    public OperandsBasedSet2 transform(final TransformationContextFromStage1To2 context) {
        return new OperandsBasedSet2(operands.stream().map(el -> el.transform(context)).collect(toList()));
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
