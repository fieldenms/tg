package ua.com.fielden.platform.eql.stage2.conditions;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class Conditions2 implements ICondition2<Conditions3> {
    public static final Conditions2 EMPTY_CONDITIONS = new Conditions2(false, ImmutableList.of());

    private final List<List<? extends ICondition2<?>>> dnf;
    private final boolean negated;

    public static Conditions2 conditions(final boolean negated, final List<List<? extends ICondition2<?>>> dnf) {
        if (dnf.isEmpty()) {
            return EMPTY_CONDITIONS;
        }
        // (OR (AND <Conditions2>)) == <Conditions2>
        else if (dnf.size() == 1 && dnf.getFirst().size() == 1 && dnf.getFirst().getFirst() instanceof Conditions2 conds) {
            // NOT cancels NOT
            return conds.withNegated(conds.negated != negated);
        }
        return new Conditions2(negated, dnf);
    }

    public Conditions2(final boolean negated, final List<List<? extends ICondition2<?>>> dnf) {
        this.dnf = ImmutableList.copyOf(dnf);
        this.negated = negated;
    }

    public Conditions2 withNegated(final boolean negated) {
        return negated == this.negated ? this : new Conditions2(negated, dnf);
    }

    @Override
    public boolean ignore() {
        return dnf.isEmpty();
    }

    @Override
    public TransformationResultFromStage2To3<Conditions3> transform(final TransformationContextFromStage2To3 context) {
        if (ignore()) {
            return new TransformationResultFromStage2To3<>(null, context);
        }

        final List<List<? extends ICondition3>> result = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;

        for (final List<? extends ICondition2<?>> andGroup : dnf) {
            final List<ICondition3> transformedAndGroup = new ArrayList<>();
            for (final ICondition2<? extends ICondition3> andGroupCondition : andGroup) {
                final TransformationResultFromStage2To3<? extends ICondition3> andGroupConditionTr = andGroupCondition.transform(currentContext);
                transformedAndGroup.add(andGroupConditionTr.item);
                currentContext = andGroupConditionTr.updatedContext;
            }
            result.add(transformedAndGroup);
        }

        return new TransformationResultFromStage2To3<>(new Conditions3(negated, result), currentContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return dnf.stream()
                .flatMap(List::stream)
                .flatMap(cond -> cond.collectProps().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        if (ignore()) {
            return emptySet();
        } else {
            final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
            for (final List<? extends ICondition2<?>> conditions : dnf) {
                for (final ICondition2<?> condition : conditions) {
                    result.addAll(condition.collectEntityTypes());
                }
            }
            return result;
        }
    }

    public boolean conditionIsSatisfied(final ICondition2<?> condition) {
        for (final List<? extends ICondition2<?>> conditions : dnf) {
            if (!conditionMatchesAnyOf(conditions, condition)) {
                return false;
            }
        }

        return !dnf.isEmpty() && !negated;
    }

    private static boolean conditionMatchesAnyOf(final List<? extends ICondition2<?>> conditions, final ICondition2<?> conditionToMatch) {
        return conditions.stream()
                .anyMatch(cond -> cond.equals(conditionToMatch)
                        || (cond instanceof Conditions2 conds && conds.conditionIsSatisfied(conditionToMatch)));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dnf.hashCode();
        result = prime * result + (negated ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Conditions2)) {
            return false;
        }

        final Conditions2 other = (Conditions2) obj;

        return Objects.equals(dnf, other.dnf) && (negated == other.negated);
    }
}
