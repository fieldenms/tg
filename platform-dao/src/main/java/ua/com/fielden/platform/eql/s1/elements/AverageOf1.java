package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.AverageOf2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;

public class AverageOf1 extends SingleOperandFunction1<AverageOf2> {
    private final boolean distinct;

    public AverageOf1(final ISingleOperand1<? extends ISingleOperand2> operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public AverageOf2 transform(final TransformatorToS2 resolver) {
        return new AverageOf2(getOperand().transform(resolver), distinct);
    }
}