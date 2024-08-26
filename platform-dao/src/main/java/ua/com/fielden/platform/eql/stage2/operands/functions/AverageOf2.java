package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.AverageOf3;

public class AverageOf2 extends SingleOperandFunction2<AverageOf3> {
    public final boolean distinct;

    public AverageOf2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean distinct) {
        super(operand, operand.type());
        this.distinct = distinct;
    }

    @Override
    public TransformationResultFromStage2To3<AverageOf3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResultFromStage2To3<>(new AverageOf3(operandTr.item, distinct, type), operandTr.updatedContext);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + (distinct ? 1231 : 1237);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AverageOf2 that
                  && distinct == that.distinct
                  && super.equals(that);
    }

}
