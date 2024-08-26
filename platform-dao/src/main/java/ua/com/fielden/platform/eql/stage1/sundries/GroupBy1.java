package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBy2;

public record GroupBy1 (ISingleOperand1<? extends ISingleOperand2<?>> operand) {

    public GroupBy2 transform(final TransformationContextFromStage1To2 context) {
        return new GroupBy2(operand.transform(context));
    }

}
