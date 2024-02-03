package ua.com.fielden.platform.eql.stage1.conditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.SetPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

/**
 * A predicate for SQL's IN / NOT IN statement.
 *
 * @author TG Team
 */
public class SetPredicate1 implements ICondition1<SetPredicate2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand;
    private final ISetOperand1<? extends ISetOperand2<?>> rightOperand;
    private final boolean negated;

    public SetPredicate1(final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand, final boolean negated, final ISetOperand1<? extends ISetOperand2<?>> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public SetPredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new SetPredicate2(leftOperand.transform(context), negated, rightOperand.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        result.addAll(leftOperand.collectEntityTypes());
        result.addAll(rightOperand.collectEntityTypes());
        return result;
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

        if (!(obj instanceof SetPredicate1)) {
            return false;
        }

        final SetPredicate1 other = (SetPredicate1) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(negated, other.negated);
    }
}