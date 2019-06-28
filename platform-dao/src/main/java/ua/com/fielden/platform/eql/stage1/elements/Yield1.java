package ua.com.fielden.platform.eql.stage1.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class Yield1 {
    public final ISingleOperand1<? extends ISingleOperand2> operand;
    public final String alias;
    public final boolean hasRequiredHint;

    public Yield1(final ISingleOperand1<? extends ISingleOperand2> operand, final String alias, final boolean hasRequiredHint) {
        this.operand = operand;
        this.alias = alias;
        this.hasRequiredHint = hasRequiredHint;
    }
    
    public Yield1(final ISingleOperand1<? extends ISingleOperand2> operand, final String alias) {
        this(operand, alias, false);
    }

    public TransformationResult<Yield2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<Yield2>(new Yield2(operandTransformationResult.item, alias, hasRequiredHint), operandTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + (hasRequiredHint ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
    
        if (!(obj instanceof Yield1)) {
            return false;
        }
        
        final Yield1 other = (Yield1) obj;

        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && (hasRequiredHint == other.hasRequiredHint);
    }
}