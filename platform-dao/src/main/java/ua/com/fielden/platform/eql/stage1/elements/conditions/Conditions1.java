package ua.com.fielden.platform.eql.stage1.elements.conditions;

import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;

public class Conditions1 implements ICondition1<Conditions2> {
    public final boolean negated;
    public final ICondition1<? extends ICondition2<?>> firstCondition;
    private final List<CompoundCondition1> otherConditions = new ArrayList<>();

    public Conditions1(final boolean negated, final ICondition1<? extends ICondition2<?>> firstCondition, final List<CompoundCondition1> otherConditions) {
        this.firstCondition = firstCondition;
        this.otherConditions.addAll(otherConditions);
        this.negated = negated;
    }

    public Conditions1() {
        negated = false;
        firstCondition = null;
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
    public TransformationResult<Conditions2> transform(final PropsResolutionContext context) {
        final List<List<ICondition1<? extends ICondition2<?>>>> dnfs = formDnf();
        final List<List<? extends ICondition2<?>>> transformed = new ArrayList<>();
        PropsResolutionContext currentResolutionContext = context;
        for (final List<ICondition1<? extends ICondition2<?>>> andGroup : dnfs) {
            final List<ICondition2<?>> transformedAndGroup = new ArrayList<>(); 
            for (final ICondition1<? extends ICondition2<?>> andGroupCondition : andGroup) {
                final TransformationResult<? extends ICondition2<?>> andGroupConditionTransformationResult = andGroupCondition.transform(currentResolutionContext);
                if (!andGroupConditionTransformationResult.item.ignore()) {
                    transformedAndGroup.add(andGroupConditionTransformationResult.item);
                    currentResolutionContext = andGroupConditionTransformationResult.updatedContext;
                }
            }
            if (!transformedAndGroup.isEmpty()) {
                transformed.add(transformedAndGroup);
            }
        }
        
//        final List<List<? extends ICondition2>> transformed = formDnf().stream()
//                .map(andGroup -> 
//                                  andGroup.stream().map(cond -> cond.transform(resolutionContext))
//                                                   .filter(cond -> !cond.ignore())
//                                                   .collect(toList())
//                    )
//                .filter(andGroup -> !andGroup.isEmpty())
//                .collect(toList());
        
        return new TransformationResult<Conditions2>(new Conditions2(negated, transformed), currentResolutionContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((firstCondition == null) ? 0 : firstCondition.hashCode());
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((otherConditions == null) ? 0 : otherConditions.hashCode());
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