package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.Floor2;

public class Floor1 extends SingleOperandFunction1<Floor2> {

    public Floor1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public Floor2 transform(final TransformationContextFromStage1To2 context) {
        return new Floor2(operand.transform(context));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + Floor1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Floor1 && super.equals(obj);
    }
}
