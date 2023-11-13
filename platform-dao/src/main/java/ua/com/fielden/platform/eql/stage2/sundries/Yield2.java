package ua.com.fielden.platform.eql.stage2.sundries;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.Yield3;

public class Yield2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    public final String alias;
    public final boolean hasNonnullableHint;

    public Yield2(final ISingleOperand2<? extends ISingleOperand3> operand, final String alias, final boolean hasNonnullableHint) {
        this.operand = operand;
        this.alias = alias;
        this.hasNonnullableHint = hasNonnullableHint;
    }

    public TransformationResultFromStage2To3<Yield3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTransformationResult = operand.transform(context);
        final TransformationContextFromStage2To3 updatedContext = operandTransformationResult.updatedContext.cloneWithNextSqlId();
        return new TransformationResultFromStage2To3<>(new Yield3(operandTransformationResult.item, alias, updatedContext.sqlId, operand.type()), updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + operand.hashCode();
        result = prime * result + (hasNonnullableHint ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Yield2)) {
            return false;
        }

        final Yield2 other = (Yield2) obj;

        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && (hasNonnullableHint == other.hasNonnullableHint);
    }
}