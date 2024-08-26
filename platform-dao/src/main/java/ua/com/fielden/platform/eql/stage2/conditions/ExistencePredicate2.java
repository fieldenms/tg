package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.queries.SubQueryForExists2;
import ua.com.fielden.platform.eql.stage3.conditions.ExistencePredicate3;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;

import java.util.Set;

public record ExistencePredicate2 (boolean negated, SubQueryForExists2 subQuery)
        implements ICondition2<ExistencePredicate3>
{

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

}
