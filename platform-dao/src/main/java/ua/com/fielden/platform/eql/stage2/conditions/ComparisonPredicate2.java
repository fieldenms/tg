package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.ComparisonPredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

public class ComparisonPredicate2 implements ICondition2<ComparisonPredicate3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final ISingleOperand2<? extends ISingleOperand3> rightOperand;
    public final ComparisonOperator operator;

    public ComparisonPredicate2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final ComparisonOperator operator, final ISingleOperand2<? extends ISingleOperand3> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore() || rightOperand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<ComparisonPredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(context);
        final TransformationResultFromStage2To3<? extends ISingleOperand3> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);
        return new TransformationResultFromStage2To3<>(new ComparisonPredicate3(leftOperandTr.item, operator, rightOperandTr.item), rightOperandTr.updatedContext);
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
        result = prime * result + operator.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ComparisonPredicate2)) {
            return false;
        }

        final ComparisonPredicate2 other = (ComparisonPredicate2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(operator, other.operator);
    }
}
