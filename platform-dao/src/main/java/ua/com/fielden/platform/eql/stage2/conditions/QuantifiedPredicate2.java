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
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

public record QuantifiedPredicate2 (ISingleOperand2<? extends ISingleOperand3> leftOperand,
                                    ComparisonOperator operator,
                                    Quantifier quantifier,
                                    SubQuery2 rightOperand)
        implements ICondition2<QuantifiedPredicate3>, ToString.IFormattable
{

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public TransformationResultFromStage2To3<QuantifiedPredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> leftOperandTr = leftOperand.transform(
                context);
        final TransformationResultFromStage2To3<SubQuery3> rightOperandTr = rightOperand.transform(
                leftOperandTr.updatedContext);

        return new TransformationResultFromStage2To3<>(
                new QuantifiedPredicate3(leftOperandTr.item, operator, quantifier, rightOperandTr.item),
                rightOperandTr.updatedContext);
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
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operator", operator)
                .add("quantifier", quantifier)
                .add("left", leftOperand)
                .add("right", rightOperand)
                .$();
    }

}
