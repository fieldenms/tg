package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.SubQuery2;
import ua.com.fielden.platform.eql.stage3.conditions.QuantifiedPredicate3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

public class QuantifiedPredicate2 implements ICondition2<QuantifiedPredicate3> {
    public final ISingleOperand2<? extends ISingleOperand3> leftOperand;
    public final SubQuery2 rightOperand;
    public final Quantifier quantifier;
    public final ComparisonOperator operator;

    public QuantifiedPredicate2(final ISingleOperand2<? extends ISingleOperand3> leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final SubQuery2 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.quantifier = quantifier;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<QuantifiedPredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(context);
        final TransformationResultFromStage2To3<SubQuery3> rightOperandTr = rightOperand.transform(leftOperandTr.updatedContext);

        return new TransformationResultFromStage2To3<>(new QuantifiedPredicate3(leftOperandTr.item, operator, quantifier, rightOperandTr.item), rightOperandTr.updatedContext);
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
        result = prime * result + quantifier.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof QuantifiedPredicate2)) {
            return false;
        }

        final QuantifiedPredicate2 other = (QuantifiedPredicate2) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(quantifier, other.quantifier) &&
                Objects.equals(operator, other.operator);
    }
}
