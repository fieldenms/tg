package ua.com.fielden.platform.eql.stage2.etc;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.etc.Yield3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class Yield2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;
    public final String alias;
    public final boolean hasRequiredHint;

    public Yield2(final ISingleOperand2<? extends ISingleOperand3> operand, final String alias, final boolean hasRequiredHint) {
        this.operand = operand;
        this.alias = alias;
        this.hasRequiredHint = hasRequiredHint;
    }

    public Class<?> javaType() {
        return operand.type();
    }

    public TransformationResult2<Yield3> transform(final TransformationContext2 context) {
        final TransformationContext2 newContext = context.cloneWithNextSqlId();
        final TransformationResult2<? extends ISingleOperand3> operandTransformationResult = operand.transform(newContext);
        return new TransformationResult2<>(new Yield3(operandTransformationResult.item, alias, newContext.sqlId, operand.type()), operandTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + operand.hashCode();
        result = prime * result + (hasRequiredHint ? 1231 : 1237);
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
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && (hasRequiredHint == other.hasRequiredHint);
    }
}