package ua.com.fielden.platform.eql.stage1.conditions;

import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;

public class Conditions1 implements ICondition1<Conditions2> {
    public static final Conditions1 emptyConditions = new Conditions1(false, null, emptyList());

    public final boolean negated;
    public final ICondition1<? extends ICondition2<?>> firstCondition;
    private final List<CompoundCondition1> otherConditions = new ArrayList<>();

    public Conditions1(final boolean negated, final ICondition1<? extends ICondition2<?>> firstCondition, final List<CompoundCondition1> otherConditions) {
        this.firstCondition = firstCondition;
        this.otherConditions.addAll(otherConditions);
        this.negated = negated;
    }

    public boolean isEmpty() {
        return firstCondition == null;
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
    public Conditions2 transform(final TransformationContext1 context) {
        if (isEmpty()) {
            return Conditions2.emptyConditions;
        }
        
        final List<List<? extends ICondition2<?>>> transformed = formDnf().stream()
                .map(andGroup -> 
                                  andGroup.stream().map(andGroupCondition -> andGroupCondition.transform(context))
                                                   .filter(andGroupConditionTransformed -> !andGroupConditionTransformed.ignore())
                                                   .collect(Collectors.toList())
                    )
                .filter(transformedAndGroup -> !transformedAndGroup.isEmpty())
                .collect(Collectors.toList());
        
        return new Conditions2(negated, transformed);
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