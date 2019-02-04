package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.SumOf2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class SumOf1 extends SingleOperandFunction1<SumOf2> {
    private final boolean distinct;

    public SumOf1(final ISingleOperand1<? extends ISingleOperand2> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public SumOf2 transform(final PropsResolutionContext resolver) {
        return new SumOf2(getOperand().transform(resolver), distinct);
    }
}