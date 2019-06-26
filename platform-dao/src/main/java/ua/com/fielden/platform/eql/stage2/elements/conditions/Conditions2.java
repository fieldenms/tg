package ua.com.fielden.platform.eql.stage2.elements.conditions;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;

public class Conditions2 extends AbstractCondition2<Conditions3> {
    private final List<List<? extends ICondition2>> allConditionsAsDnf = new ArrayList<>();
    private final boolean negated;

    public Conditions2(final boolean negated, final List<List<? extends ICondition2>> allConditions) {
        this.allConditionsAsDnf.addAll(allConditions);
        this.negated = negated;
    }
    
    public Conditions2() {
        this(false, emptyList());
    }

    @Override
    public boolean ignore() {
        return allConditionsAsDnf.isEmpty();
    }

    @Override
    public TransformationResult<Conditions3> transform(final TransformationContext transformationContext) {
        final List<List<? extends ICondition3>> result = new ArrayList<>();
        TransformationContext currentResolutionContext = transformationContext;
        
        for (final List<? extends ICondition2> andGroup : allConditionsAsDnf) {
            final List<ICondition3> transformedAndGroup = new ArrayList<>(); 
            for (final ICondition2<? extends ICondition3> andGroupCondition : andGroup) {
                final TransformationResult<? extends ICondition3> andGroupConditionTransformationResult = andGroupCondition.transform(currentResolutionContext);
                transformedAndGroup.add(andGroupConditionTransformationResult.getItem());
                currentResolutionContext = andGroupConditionTransformationResult.getUpdatedContext();
            }
            result.add(transformedAndGroup);
        }
        
//        final List<List<? extends ICondition2>> transformed = formDnf().stream()
//                .map(andGroup -> 
//                                  andGroup.stream().map(cond -> cond.transform(resolutionContext))
//                                                   .filter(cond -> !cond.ignore())
//                                                   .collect(toList())
//                    )
//                .filter(andGroup -> !andGroup.isEmpty())
//                .collect(toList());
        
        return new TransformationResult<Conditions3>(new Conditions3(negated, result), currentResolutionContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((allConditionsAsDnf == null) ? 0 : allConditionsAsDnf.hashCode());
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
        
        return Objects.equals(allConditionsAsDnf, other.allConditionsAsDnf) && Objects.equals(negated, other.negated);
    }
}