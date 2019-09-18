package ua.com.fielden.platform.eql.stage2.elements.functions;

import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;
import ua.com.fielden.platform.eql.stage3.elements.functions.CaseWhen3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;
import ua.com.fielden.platform.types.tuples.T2;

public class CaseWhen2 extends AbstractFunction2<CaseWhen3> {

    private List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> whenThenPairs = new ArrayList<>();
    private final ISingleOperand2<? extends ISingleOperand3> elseOperand;
    private final ITypeCast typeCast;

    public CaseWhen2(final List<T2<ICondition2<? extends ICondition3>, ISingleOperand2<? extends ISingleOperand3>>> whenThenPairs, final ISingleOperand2<? extends ISingleOperand3> elseOperand, final ITypeCast typeCast) {
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
    }

    @Override
    public Class<?> type() {
        // TODO EQL
        return whenThenPairs.get(0)._2.type();
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
        final TransformationResult<? extends ISingleOperand3> elseOperandTr = elseOperand.transform(currentContext);
        
        return new TransformationResult<CaseWhen3>(new CaseWhen3(transformedWhenThenPairs, elseOperandTr.item, typeCast), elseOperandTr.updatedContext);
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