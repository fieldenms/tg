package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBy2;
import ua.com.fielden.platform.utils.ToString;

public record GroupBy1 (ISingleOperand1<? extends ISingleOperand2<?>> operand) implements ToString.IFormattable {

    public GroupBy2 transform(final TransformationContextFromStage1To2 context) {
        return new GroupBy2(operand.transform(context));
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("operand", operand)
                .$();
    }

}
