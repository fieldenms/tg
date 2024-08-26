package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.AddDateInterval2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

import java.util.Objects;

public class AddDateInterval1 extends TwoOperandsFunction1<AddDateInterval2> {
    
    private final DateIntervalUnit intervalUnit;

    public AddDateInterval1(final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand1, final DateIntervalUnit intervalUnit, final ISingleOperand1<? extends ISingleOperand2<? extends ISingleOperand3>> operand2) {
        super(operand1, operand2);
        this.intervalUnit = intervalUnit;
    }

    @Override
    public AddDateInterval2 transform(final TransformationContextFromStage1To2 context) {
        return new AddDateInterval2(operand1.transform(context), intervalUnit, operand2.transform(context));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + intervalUnit.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof AddDateInterval1 that && Objects.equals(intervalUnit, that.intervalUnit);
    }

}
