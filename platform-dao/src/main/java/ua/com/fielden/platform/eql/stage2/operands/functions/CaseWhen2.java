package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.CaseWhen3;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ToString;

import java.util.*;

import static ua.com.fielden.platform.types.tuples.T2.t2;

public class CaseWhen2 extends AbstractFunction2<CaseWhen3> {

    private final List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> whenThenPairs = new ArrayList<>();
    private final ISingleOperand2<? extends ISingleOperand3> elseOperand;
    private final ITypeCast typeCast;

    public CaseWhen2(final List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> whenThenPairs, final ISingleOperand2<? extends ISingleOperand3> elseOperand, final ITypeCast typeCast) {
        super(extractTypes(whenThenPairs, elseOperand));
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
    }

    private static Set<PropType> extractTypes(final List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> whenThenPairs, final ISingleOperand2<? extends ISingleOperand3> elseOperand) {
        final Set<PropType> types = new HashSet<>();

        if (elseOperand != null) {
            types.add(elseOperand.type());    
        }

        whenThenPairs.stream().map(pair -> pair._2.type()).forEach(types::add);

        return types;
    }

    @Override
    public TransformationResultFromStage2To3<CaseWhen3> transform(final TransformationContextFromStage2To3 context) {
        final List<T2<ICondition3, ISingleOperand3>> transformedWhenThenPairs = new ArrayList<>();
        TransformationContextFromStage2To3 currentContext = context;
        for (final T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>> pair : whenThenPairs) {
            final TransformationResultFromStage2To3<? extends ICondition3> conditionTr = pair._1.transform(currentContext);
            currentContext = conditionTr.updatedContext;
            final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = pair._2.transform(currentContext);
            currentContext = operandTr.updatedContext;
            transformedWhenThenPairs.add(t2(conditionTr.item, operandTr.item));
        }
        final TransformationResultFromStage2To3<? extends ISingleOperand3> elseOperandTr = elseOperand == null ? null : elseOperand.transform(currentContext);
        
        return new TransformationResultFromStage2To3<>(new CaseWhen3(transformedWhenThenPairs, elseOperandTr == null ? null : elseOperandTr.item, typeCast, type), elseOperandTr == null ? currentContext : elseOperandTr.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>();
        for (final T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>> pair : whenThenPairs) {
            result.addAll(pair._1.collectProps());
            result.addAll(pair._2.collectProps());
        }
        if (elseOperand != null) {
            result.addAll(elseOperand.collectProps());    
        }
        
        return result;
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        final Set<Class<? extends AbstractEntity<?>>> result = new HashSet<>();
        for (final T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>> pair : whenThenPairs) {
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
               || obj instanceof CaseWhen2 that
                  && Objects.equals(whenThenPairs, that.whenThenPairs)
                  && Objects.equals(elseOperand, that.elseOperand)
                  && Objects.equals(typeCast, that.typeCast);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("whenThenPairs", whenThenPairs)
                .addIfNotNull("else", elseOperand)
                .addIfNotNull("typeCast", typeCast);
    }

}
