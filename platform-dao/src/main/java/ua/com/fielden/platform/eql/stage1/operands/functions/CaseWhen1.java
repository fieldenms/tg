package ua.com.fielden.platform.eql.stage1.operands.functions;

import com.google.common.collect.ImmutableList;
import jakarta.annotation.Nullable;
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
import ua.com.fielden.platform.utils.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.types.tuples.T2.t2;

public record CaseWhen1 (List<T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThenPairs,
                         @Nullable ISingleOperand1<? extends ISingleOperand2<?>> elseOperand,
                         ITypeCast typeCast)
        implements IFunction1<CaseWhen2>, ToString.IFormattable
{

    public CaseWhen1(final List<T2<ICondition1<? extends ICondition2<?>>, ISingleOperand1<? extends ISingleOperand2<?>>>> whenThenPairs,
                     final ISingleOperand1<? extends ISingleOperand2<?>> elseOperand,
                     final ITypeCast typeCast)
    {
        this.whenThenPairs = ImmutableList.copyOf(whenThenPairs);
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
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("whenThenPairs", whenThenPairs)
                .addIfNotNull("else", elseOperand)
                .addIfNotNull("typeCast", typeCast)
                .$();
    }

}
