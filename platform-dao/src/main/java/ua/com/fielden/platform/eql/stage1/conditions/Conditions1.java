package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
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
public class Conditions1 implements ICondition1<Conditions2> {
    public static final Conditions1 EMPTY_CONDITIONS = new Conditions1(false, null, emptyList());
    private static final ComparisonPredicate1 ID_EQUALS_EXT_ID_CONDITION = new ComparisonPredicate1(new Prop1(ID, false), EQ, new Prop1(ID, true));

    public final boolean negated;
    public final ICondition1<? extends ICondition2<?>> firstCondition;
    private final List<CompoundCondition1> otherConditions = new ArrayList<>();

    public Conditions1(final boolean negated, final ICondition1<? extends ICondition2<?>> firstCondition, final List<CompoundCondition1> otherConditions) {
        this.firstCondition = firstCondition;
        this.otherConditions.addAll(otherConditions);
        this.negated = negated;
    }

    public static Conditions1 conditions(final ICondition1<? extends ICondition2<?>> condition) {
        // can we avoid redundant wrapping?
        if (condition instanceof Conditions1 conditions1 && !conditions1.negated) {
            return conditions1;
        }
        return new Conditions1(false, condition, List.of());
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
        return new Conditions1(false, conditions.getFirst(), conditions.stream().skip(1).map(c -> new CompoundCondition1(operator, c)).toList());
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

    private List<List<ICondition1<? extends ICondition2<?>>>> formDnf() {
        final List<List<ICondition1<? extends ICondition2<?>>>> dnf = new ArrayList<>();
        List<ICondition1<? extends ICondition2<?>>> andGroup = new ArrayList<>();

        if (firstCondition != null) {
            andGroup.add(firstCondition);
        }

        for (final CompoundCondition1 compoundCondition : otherConditions) {
            if (compoundCondition.logicalOperator == AND) {
                andGroup.add(compoundCondition.condition);
            } else {
                if (!andGroup.isEmpty()) {
                    dnf.add(andGroup);
                }

                andGroup = new ArrayList<>();
                andGroup.add(compoundCondition.condition);
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
                                .toList())
                .filter(transformedAndGroup -> !transformedAndGroup.isEmpty())
                .collect(toList());

        return transformed.isEmpty() ? Conditions2.EMPTY_CONDITIONS : new Conditions2(negated, transformed);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        if (isEmpty()) {
            return emptySet();
        } else {
            final Set<Class<? extends AbstractEntity<?>>> result = otherConditions.stream().map(el -> el.condition.collectEntityTypes()).flatMap(Set::stream).collect(toSet());
            result.addAll(firstCondition.collectEntityTypes());
            return result;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstCondition == null) ? 0 : firstCondition.hashCode());
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + otherConditions.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Conditions1)) {
            return false;
        }

        final Conditions1 other = (Conditions1) obj;

        return Objects.equals(firstCondition, other.firstCondition) &&
                Objects.equals(otherConditions, other.otherConditions) &&
                Objects.equals(negated, other.negated);
    }
}
