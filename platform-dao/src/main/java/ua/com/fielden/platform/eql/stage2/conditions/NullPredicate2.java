package ua.com.fielden.platform.eql.stage2.conditions;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.conditions.NullPredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class NullPredicate2 implements ICondition2<NullPredicate3> {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    private final boolean negated;

    public NullPredicate2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NullPredicate2)) {
            return false;
        }

        final NullPredicate2 other = (NullPredicate2) obj;

        return (negated == other.negated) && Objects.equals(operand, other.operand);
    }
}