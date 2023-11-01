package ua.com.fielden.platform.eql.stage1.conditions;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.queries.SubQueryForExists1;
import ua.com.fielden.platform.eql.stage2.conditions.ExistencePredicate2;

/**
 * A predicate for SQL's EXISTS / NOT EXISTS statement.
 *
 * @author TG Team
 */
public class ExistencePredicate1 implements ICondition1<ExistencePredicate2> {
    private final boolean negated;
    private final SubQueryForExists1 subQuery;

    public ExistencePredicate1(final boolean negated, final SubQueryForExists1 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public ExistencePredicate2 transform(final TransformationContext1 context) {
        return new ExistencePredicate2(negated, subQuery.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return subQuery.collectEntityTypes();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + subQuery.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ExistencePredicate1)) {
            return false;
        }

        final ExistencePredicate1 other = (ExistencePredicate1) obj;

        return Objects.equals(negated, other.negated) && Objects.equals(subQuery, other.subQuery);
    }
}