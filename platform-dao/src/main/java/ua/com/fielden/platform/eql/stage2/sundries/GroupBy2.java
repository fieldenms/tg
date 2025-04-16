package ua.com.fielden.platform.eql.stage2.sundries;

import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBy3;
import ua.com.fielden.platform.utils.ToString;

public record GroupBy2 (ISingleOperand2<? extends ISingleOperand3> operand) implements ToString.IFormattable {

    public TransformationResultFromStage2To3<GroupBy3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResultFromStage2To3<>(new GroupBy3(operandTr.item), operandTr.updatedContext);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operand", operand)
                .$();
    }

}
