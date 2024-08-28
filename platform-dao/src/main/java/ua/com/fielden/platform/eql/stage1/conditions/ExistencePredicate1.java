package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.queries.SubQueryForExists1;
import ua.com.fielden.platform.eql.stage2.conditions.ExistencePredicate2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

/**
 * A predicate for SQL's EXISTS / NOT EXISTS statement.
 *
 * @author TG Team
 */
public record ExistencePredicate1 (boolean negated, SubQueryForExists1 subQuery)
        implements ICondition1<ExistencePredicate2>, ToString.IFormattable
{

    @Override
    public ExistencePredicate2 transform(final TransformationContextFromStage1To2 context) {
        return new ExistencePredicate2(negated, subQuery.transform(context));
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return subQuery.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("subQuery", subQuery)
                .add("negated", negated)
                .$();
    }

}
