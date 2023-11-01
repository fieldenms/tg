package ua.com.fielden.platform.eql.stage1.conditions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.LikePredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

/**
 * A predicate for SQL's LIKE / NOT LIKE statement.
 * Additionally holds information about case sensitivity.
 *
 * @author TG Team
 */
public class LikePredicate1 implements ICondition1<LikePredicate2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand;
    private final LikeOptions options;

    public LikePredicate1(final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand, final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand, final LikeOptions options) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.options = options;
    }

    @Override
    public LikePredicate2 transform(final TransformationContext1 context) {
        return new LikePredicate2(leftOperand.transform(context), rightOperand.transform(context), options);
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
        result = prime * result + options.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LikePredicate1)) {
            return false;
        }

        final LikePredicate1 other = (LikePredicate1) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(options, other.options);
    }
}