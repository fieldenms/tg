package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.NullPredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

public record NullPredicate2 (ISingleOperand2<? extends ISingleOperand3> operand, boolean negated)
        implements ICondition2<NullPredicate3>, ToString.IFormattable
{

    @Override
    public boolean ignore() {
        return operand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<NullPredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResultFromStage2To3<>(new NullPredicate3(operandTr.item, negated), operandTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return operand.collectProps();
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operand", operand)
                .add("negated", negated)
                .$();
    }

}
