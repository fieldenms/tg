package ua.com.fielden.platform.eql.stage2.conditions;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.SubQueryForExists2;
import ua.com.fielden.platform.eql.stage3.conditions.ExistencePredicate3;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;

public class ExistencePredicate2 implements ICondition2<ExistencePredicate3> {
    private final boolean negated;
    private final SubQueryForExists2 subQuery;

    public ExistencePredicate2(final boolean negated, final SubQueryForExists2 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public TransformationResultFromStage2To3<ExistencePredicate3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<SubQueryForExists3> subQueryTr = subQuery.transform(context);
        return new TransformationResultFromStage2To3<>(new ExistencePredicate3(negated, subQueryTr.item), subQueryTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return subQuery.collectProps();
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

        if (!(obj instanceof ExistencePredicate2)) {
            return false;
        }

        final ExistencePredicate2 other = (ExistencePredicate2) obj;

        return Objects.equals(subQuery, other.subQuery) && (negated == other.negated);
    }
}