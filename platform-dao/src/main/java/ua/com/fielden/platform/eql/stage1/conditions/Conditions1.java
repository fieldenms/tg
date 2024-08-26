package ua.com.fielden.platform.eql.stage1.conditions;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.max;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;

/**
 * Represents a group of conditions combined with logical OR or AND.
 *
 * Conditions are analysed for the subject of being ignored. Conditions get ignored if their operand(s) are empty ({@code iVal(null)} or {@code iParam(null)}).
 * In order to determine, which conditions can be ignored a DNF-like structure is built (disjunction of conjunctions).
 *
 * @author TG Team
 *
 */
public record Conditions1 (boolean negated,
                           ICondition1<? extends ICondition2<?>> firstCondition,
                           List<CompoundCondition1> otherConditions)
        implements ICondition1<Conditions2>
{

    public static final Conditions1 EMPTY_CONDITIONS = new Conditions1(false, null, ImmutableList.of());
    private static final ComparisonPredicate1 ID_EQUALS_EXT_ID_CONDITION = new ComparisonPredicate1(new Prop1(ID, false), EQ, new Prop1(ID, true));

    public Conditions1(final boolean negated, final ICondition1<? extends ICondition2<?>> firstCondition, final List<CompoundCondition1> otherConditions) {
        this.firstCondition = firstCondition;
        this.otherConditions = ImmutableList.copyOf(otherConditions);
        this.negated = negated;
    }

    public static Conditions1 conditions(final ICondition1<? extends ICondition2<?>> condition) {
        // can we avoid redundant wrapping?
        if (condition instanceof Conditions1 conditions1) {
            return !conditions1.negated
                    ? conditions1
                    : new Conditions1(true, conditions1.firstCondition, conditions1.otherConditions);
        }
        return new Conditions1(false, condition, ImmutableList.of());
    }

    @SafeVarargs
    public static Conditions1 conditions(final LogicalOperator operator, final ICondition1<? extends ICondition2<?>>... conditions) {
        return conditions(operator, Arrays.asList(conditions));
    }

    public static Conditions1 conditions(final LogicalOperator operator, final List<? extends ICondition1<? extends ICondition2<?>>> conditions) {
        if (conditions.isEmpty()) {
            return EMPTY_CONDITIONS;
        }
        else if (conditions.size() == 1) {
            // operator is unnecessary for a single condition
            return conditions(conditions.getFirst());
        }
        return new Conditions1(
                false,
                conditions.getFirst(),
                conditions.stream().skip(1).map(c -> new CompoundCondition1(operator, c)).collect(toImmutableList()));
    }

    @SafeVarargs
    public static Conditions1 and(final ICondition1<? extends ICondition2<?>>... conditions) {
        return conditions(AND, conditions);
    }

    public static Conditions1 and(final List<? extends ICondition1<? extends ICondition2<?>>> conditions) {
        return conditions(AND, conditions);
    }

    @SafeVarargs
    public static Conditions1 or(final ICondition1<? extends ICondition2<?>>... conditions) {
        return conditions(OR, conditions);
    }

    public static Conditions1 or(final List<? extends ICondition1<? extends ICondition2<?>>> conditions) {
        return conditions(OR, conditions);
    }

    public Conditions1 negate() {
        return new Conditions1(!negated, firstCondition, otherConditions);
    }

    public boolean isEmpty() {
        return firstCondition == null;
    }

    public boolean isIdEqualsExtId() {
        return !negated && otherConditions.isEmpty() && ID_EQUALS_EXT_ID_CONDITION.equals(firstCondition);
    }

    private int size() {
        return otherConditions.size() + (firstCondition == null ? 0 : 1);
    }

    private List<List<ICondition1<? extends ICondition2<?>>>> formDnf() {
        final List<List<ICondition1<? extends ICondition2<?>>>> dnf = new ArrayList<>(max(1, size() / 2));
        final int andGroupEstSize = max(1, size() / 3);
        List<ICondition1<? extends ICondition2<?>>> andGroup = new ArrayList<>(andGroupEstSize);

        if (firstCondition != null) {
            andGroup.add(firstCondition);
        }

        for (final CompoundCondition1 compoundCondition : otherConditions) {
            if (compoundCondition.logicalOperator() == AND) {
                andGroup.add(compoundCondition.condition());
            } else {
                if (!andGroup.isEmpty()) {
                    dnf.add(andGroup);
                }

                andGroup = new ArrayList<>(andGroupEstSize);
                andGroup.add(compoundCondition.condition());
            }
        }

        if (!andGroup.isEmpty()) {
            dnf.add(andGroup);
        }

        return dnf;
    }

    @Override
    public Conditions2 transform(final TransformationContextFromStage1To2 context) {
        if (isEmpty()) {
            return Conditions2.EMPTY_CONDITIONS;
        }

        final List<List<? extends ICondition2<?>>> transformed = formDnf().stream()
                .map(andGroup ->
                             andGroup.stream().map(andGroupCondition -> andGroupCondition.transform(context))
                                     .filter(andGroupConditionTransformed -> !andGroupConditionTransformed.ignore())
                                     .collect(toImmutableList()))
                .filter(transformedAndGroup -> !transformedAndGroup.isEmpty())
                .collect(toImmutableList());

        return Conditions2.conditions(negated, transformed);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        if (isEmpty()) {
            return emptySet();
        } else {
            final Set<Class<? extends AbstractEntity<?>>> result = otherConditions.stream()
                    .map(el -> el.condition().collectEntityTypes())
                    .flatMap(Set::stream)
                    .collect(toSet());
            result.addAll(firstCondition.collectEntityTypes());
            return result;
        }
    }

}
