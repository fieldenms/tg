package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.SetPredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISetOperand3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

public class SetPredicate2 implements ICondition2<SetPredicate3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final ISetOperand2<? extends ISetOperand3> rightOperand;
    public final boolean negated;

    public SetPredicate2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final boolean negated, final ISetOperand2<? extends ISetOperand3> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<SetPredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(context);
        final TransformationResultFromStage2To3<? extends ISetOperand3> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);
        return new TransformationResultFromStage2To3<>(new SetPredicate3(leftOperandTr.item, negated, rightOperandTr.item), rightOperandTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        result.addAll(leftOperand.collectProps());
        result.addAll(rightOperand.collectProps());
        return result;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return concat(HashSet::new, leftOperand.collectEntityTypes(), rightOperand.collectEntityTypes());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SetPredicate2)) {
            return false;
        }

        final SetPredicate2 other = (SetPredicate2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                (negated == other.negated);
    }
}
