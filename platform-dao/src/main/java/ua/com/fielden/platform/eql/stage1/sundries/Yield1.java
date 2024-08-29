package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;

import java.util.Objects;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;

public class Yield1 {
    public static final String ABSENT_ALIAS = ""; // Used for the cases where yield requires no alias (sub-query with single yield).

    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final String alias;
    public final boolean hasNonnullableHint;

    public Yield1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final String alias, final boolean hasNonnullableHint) {
        requireNotNullArgument(alias, "alias");
        this.operand = operand;
        this.alias = alias;
        this.hasNonnullableHint = hasNonnullableHint;
    }

    public Yield1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        this(operand, ABSENT_ALIAS, false);
    }

    public Yield2 transform(final TransformationContextFromStage1To2 context) {
        return new Yield2(operand.transform(context), alias, hasNonnullableHint);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + alias.hashCode();
        result = prime * result + (hasNonnullableHint ? 1231 : 1237);
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

        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && (hasNonnullableHint == other.hasNonnullableHint);
    }
}
