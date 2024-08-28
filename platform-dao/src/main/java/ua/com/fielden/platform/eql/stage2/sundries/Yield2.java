package ua.com.fielden.platform.eql.stage2.sundries;

import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;
import ua.com.fielden.platform.utils.ToString;

public record Yield2(ISingleOperand2<? extends ISingleOperand3> operand, String alias, boolean hasNonnullableHint)
        implements ToString.IFormattable
{

    public TransformationResultFromStage2To3<Yield3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTransformationResult = operand.transform(
                context);
        final TransformationContextFromStage2To3 updatedContext = operandTransformationResult.updatedContext.cloneWithNextSqlId();
        return new TransformationResultFromStage2To3<>(
                new Yield3(operandTransformationResult.item, alias, updatedContext.sqlId, operand.type()),
                updatedContext);
    }

        @Override
        public String toString() {
            return toString(ToString.separateLines);
        }

        @Override
        public String toString(final ToString.IFormat format) {
            return format.toString(this)
                    .add("operand", operand)
                    .add("alias", alias)
                    .add("hasNonnullableHint", hasNonnullableHint)
                    .$();
        }

}
