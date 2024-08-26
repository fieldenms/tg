package ua.com.fielden.platform.eql.stage1.conditions;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.conditions.NullPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.eql.stage2.conditions.Conditions2.conditions;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

/**
 * A predicate for SQL's IS NULL / IS NOT NULL statement.
 * Supports operands of union type by checking every member for nullability.
 *
 * @author TG Team
 */
public record NullPredicate1 (ISingleOperand1<? extends ISingleOperand2<?>> operand, boolean negated)
        implements ICondition1<ICondition2<?>>
{

    @Override
    public ICondition2<?> transform(final TransformationContextFromStage1To2 context) {
        final ISingleOperand2<?> transformedOperand = operand.transform(context);
        if (transformedOperand instanceof Prop2 prop && isUnionEntityType(prop.type.javaType())) {
            final QuerySourceItemForUnionType<?> lastResolutionItem = (QuerySourceItemForUnionType<?>) prop.getPath().getLast();

            final Stream<ICondition2<?>> nullPredicates = lastResolutionItem.getProps().values().stream()
                    .filter(item -> !item.hasExpression())
                    .map(item -> {
                        final var subPropPath = ImmutableList.<AbstractQuerySourceItem<?>>builder()
                                .addAll(prop.getPath()).add(item).build();
                        return new NullPredicate2(new Prop2(prop.source, subPropPath), negated);
                    });

            if (negated) {
                final List<List<? extends ICondition2<?>>> negatedNullTests = nullPredicates.map(ImmutableList::of).collect(toImmutableList());
                return conditions(false, negatedNullTests);
            } else {
                return conditions(false, ImmutableList.of(nullPredicates.collect(toImmutableList())));
            }
        }

        return new NullPredicate2(transformedOperand, negated);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }

}
