package ua.com.fielden.platform.eql.stage2.operands.functions;

import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.CaseWhen3;
import ua.com.fielden.platform.types.tuples.T2;

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

    private static Set<Class<?>> extractTypes(final List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> whenThenPairs, final ISingleOperand2<? extends ISingleOperand3> elseOperand) {
        final Set<Class<?>> types = new HashSet<>();
        if (elseOperand != null) {
            types.add(elseOperand.type());    
        }
        
        for (final T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>> item : whenThenPairs) {
            types.add(item._2.type());
        }
        
        return types;
    }

    @Override
    public TransformationResult<CaseWhen3> transform(final TransformationContext context) {
        final List<T2<ICondition3, ISingleOperand3>> transformedWhenThenPairs = new ArrayList<>();
        TransformationContext currentContext = context;
        for (final T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>> pair : whenThenPairs) {
            final TransformationResult<? extends ICondition3> conditionTr = pair._1.transform(currentContext);
            currentContext = conditionTr.updatedContext;
            final TransformationResult<? extends ISingleOperand3> operandTr = pair._2.transform(currentContext);
            currentContext = operandTr.updatedContext;
            transformedWhenThenPairs.add(t2(conditionTr.item, operandTr.item));
        }
        final TransformationResult<? extends ISingleOperand3> elseOperandTr = elseOperand == null ? null : elseOperand.transform(currentContext);
        
        return new TransformationResult<CaseWhen3>(new CaseWhen3(transformedWhenThenPairs, elseOperandTr == null ? null : elseOperandTr.item, typeCast, type, hibType), elseOperandTr == null ? currentContext : elseOperandTr.updatedContext);
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
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof CaseWhen2)) {
            return false;
        }

        final CaseWhen2 other = (CaseWhen2) obj;

        return Objects.equals(whenThenPairs, other.whenThenPairs) && Objects.equals(elseOperand, other.elseOperand) && Objects.equals(typeCast, other.typeCast);
    }
}