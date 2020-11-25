package ua.com.fielden.platform.eql.stage1.elements.core;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.core.Yield2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class Yield1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final String alias;
    public final boolean hasRequiredHint;

    public Yield1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final String alias, final boolean hasRequiredHint) {
        this.operand = operand;
        this.alias = alias;
        this.hasRequiredHint = hasRequiredHint;
    }
    
    public Yield2 transform(final PropsResolutionContext context) {
        return new Yield2(operand.transform(context), alias, hasRequiredHint);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + (hasRequiredHint ? 1231 : 1237);
        result = prime * result + operand.hashCode();
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