package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.CaseWhen2;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.*;

import static ua.com.fielden.platform.types.tuples.T2.t2;

public class CaseWhen1 extends AbstractFunction1<CaseWhen2> {

    private List<T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThenPairs = new ArrayList<>();
    /** Can be null. */
    public final ISingleOperand1<? extends ISingleOperand2<?>> elseOperand;
    private final ITypeCast typeCast;

    public CaseWhen1(final List<T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThenPairs, final ISingleOperand1<? extends ISingleOperand2<?>> elseOperand, final ITypeCast typeCast) {
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
    }

    @Override
    public CaseWhen2 transform(final TransformationContextFromStage1To2 context) {
        final List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> transformedWhenThenPairs = new ArrayList<>();
        for (final T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>> pair : whenThenPairs) {
            final ICondition2<?> conditionTransformed = pair._1.transform(context);
            final ISingleOperand2<?> operandTransformed = pair._2.transform(context);
            transformedWhenThenPairs.add(t2(conditionTransformed, operandTransformed));
        }
        final ISingleOperand2<?> elseOperandTransformed = elseOperand == null ? null : elseOperand.transform(context);
        
        return new CaseWhen2(transformedWhenThenPairs, elseOperandTransformed, typeCast);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        for (final T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>> pair : whenThenPairs) {
            result.addAll(pair._1.collectEntityTypes());
            result.addAll(pair._2.collectEntityTypes());
        }
        if (elseOperand != null) {
            result.addAll(elseOperand.collectEntityTypes());    
        }
        
        return result;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elseOperand == null) ? 0 : elseOperand.hashCode());
        result = prime * result + ((typeCast == null) ? 0 : typeCast.hashCode());
        result = prime * result + whenThenPairs.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof CaseWhen1 that
                  && Objects.equals(whenThenPairs, that.whenThenPairs)
                  && Objects.equals(elseOperand, that.elseOperand)
                  && Objects.equals(typeCast, that.typeCast);
    }

}
