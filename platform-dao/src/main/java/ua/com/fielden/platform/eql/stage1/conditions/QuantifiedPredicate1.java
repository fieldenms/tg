package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.conditions.QuantifiedPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ua.com.fielden.platform.utils.CollectionUtil.concat;

/**
 * A predicate for SQL's ANY / ALL statement (i.e., {@code WHERE propX > ALL (SELECT someprop FROM ....)} .
 *
 * @author TG Team
 */
public class QuantifiedPredicate1 implements ICondition1<QuantifiedPredicate2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand;
    private final SubQuery1 rightOperand;
    private final Quantifier quantifier;
    private final ComparisonOperator operator;

    public QuantifiedPredicate1(final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final SubQuery1 rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
        this.quantifier = quantifier;
    }

    @Override
    public QuantifiedPredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new QuantifiedPredicate2(leftOperand.transform(context), operator, quantifier, rightOperand.transform(context));
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

        if (!(obj instanceof QuantifiedPredicate1)) {
            return false;
        }

        final QuantifiedPredicate1 other = (QuantifiedPredicate1) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(quantifier, other.quantifier) &&
                Objects.equals(operator, other.operator);
    }
}
