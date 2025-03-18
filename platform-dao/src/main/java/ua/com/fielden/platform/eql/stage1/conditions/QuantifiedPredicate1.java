package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.conditions.QuantifiedPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.HashSet;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

/**
 * A predicate for SQL's ANY / ALL statement (i.e., {@code WHERE propX > ALL (SELECT someprop FROM ....)} .
 *
 * @author TG Team
 */
public record QuantifiedPredicate1 (ISingleOperand1<? extends ISingleOperand2<?>> leftOperand,
                                    ComparisonOperator operator,
                                    Quantifier quantifier,
                                    SubQuery1 rightOperand)
        implements ICondition1<QuantifiedPredicate2>, ToString.IFormattable
{

    @Override
    public QuantifiedPredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new QuantifiedPredicate2(leftOperand.transform(context), operator, quantifier, rightOperand.transform(context));
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
