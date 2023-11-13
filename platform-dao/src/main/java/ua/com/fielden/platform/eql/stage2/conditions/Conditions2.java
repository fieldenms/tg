package ua.com.fielden.platform.eql.stage2.conditions;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;

public class Conditions2 implements ICondition2<Conditions3> {
    public static final Conditions2 EMPTY_CONDITIONS = new Conditions2(false, emptyList());

    private final List<List<? extends ICondition2<?>>> allConditionsAsDnf = new ArrayList<>();
    private final boolean negated;

    public Conditions2(final boolean negated, final List<List<? extends ICondition2<?>>> allConditions) {
        this.allConditionsAsDnf.addAll(allConditions);
        this.negated = negated;
    }

    @Override
    public boolean ignore() {
        return allConditionsAsDnf.isEmpty();
    }

    @Override
    public TransformationResultFromStage2To3<Conditions3> transform(final TransformationContextFromStage2To3 context) {
        if (ignore()) {
            return new TransformationResultFromStage2To3<>(null, context);
        }

        final List<List<? extends ICondition3>> result = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;

        for (final List<? extends ICondition2<?>> andGroup : allConditionsAsDnf) {
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
        final Set<Prop2> result = new HashSet<>();
        for (final List<? extends ICondition2<?>> list : allConditionsAsDnf) {
            for (final ICondition2<?> cond : list) {
                result.addAll(cond.collectProps());
            }
        }
        return result;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        if (ignore()) {
            return emptySet();
        } else {
            final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
            for (final List<? extends ICondition2<?>> conditions : allConditionsAsDnf) {
                for (final ICondition2<?> condition : conditions) {
                    result.addAll(condition.collectEntityTypes());
                }
            }
            return result;
        }
    }

    public boolean conditionIsSatisfied(final ICondition2<?> condition) {
        for (final List<? extends ICondition2<?>> conditions : allConditionsAsDnf) {
            if (!conditionMatchesAnyOf(conditions, condition)) {
                return false;
            }
        }

        return allConditionsAsDnf.isEmpty() || negated ? false : true;
    }

    private boolean conditionMatchesAnyOf(final List<? extends ICondition2<?>> conditions, final ICondition2<?> conditionToMatch) {
        for (final ICondition2<?> condition : conditions) {
            if (condition.equals(conditionToMatch) || (condition instanceof Conditions2 && ((Conditions2) condition).conditionIsSatisfied(conditionToMatch))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + allConditionsAsDnf.hashCode();
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

        return Objects.equals(allConditionsAsDnf, other.allConditionsAsDnf) && (negated == other.negated);
    }
}