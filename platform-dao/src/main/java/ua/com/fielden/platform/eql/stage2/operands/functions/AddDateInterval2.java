package ua.com.fielden.platform.eql.stage2.operands.functions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.AddDateInterval3;
import ua.com.fielden.platform.utils.ToString;

public class AddDateInterval2 extends TwoOperandsFunction2<AddDateInterval3> {
    private final DateIntervalUnit intervalUnit;
    
    public AddDateInterval2(final ISingleOperand2<? extends ISingleOperand3> operand1, final DateIntervalUnit intervalUnit, final ISingleOperand2<? extends ISingleOperand3> operand2) {
        super(operand1, operand2, operand2.type());
        this.intervalUnit = intervalUnit;
    }

    @Override
    public TransformationResultFromStage2To3<AddDateInterval3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> firstOperandTr = operand1.transform(context);
        final TransformationResultFromStage2To3<? extends ISingleOperand3> secondOperandTr = operand2.transform(firstOperandTr.updatedContext);
        return new TransformationResultFromStage2To3<>(new AddDateInterval3(firstOperandTr.item, intervalUnit, secondOperandTr.item, type), secondOperandTr.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + intervalUnit.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AddDateInterval2 that
                  && Objects.equals(intervalUnit, that.intervalUnit)
                  && super.equals(that);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("unit", intervalUnit);
    }

}
