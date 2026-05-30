package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.Ceil2;

public class Ceil1 extends SingleOperandFunction1<Ceil2> {

    public Ceil1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        super(operand);
    }

    @Override
    public Ceil2 transform(final TransformationContextFromStage1To2 context) {
        return new Ceil2(operand.transform(context));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + Ceil1.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Ceil1 && super.equals(obj);
    }
}
