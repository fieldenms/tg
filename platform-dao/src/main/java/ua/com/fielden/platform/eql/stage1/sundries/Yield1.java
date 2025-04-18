package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;
import ua.com.fielden.platform.utils.ToString;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;

public record Yield1 (ISingleOperand1<? extends ISingleOperand2<?>> operand,
                      String alias,
                      boolean hasNonnullableHint)
    implements ToString.IFormattable
{

    public static final String ABSENT_ALIAS = ""; // Used for the cases where yield requires no alias (sub-query with single yield).

    public Yield1 {
        requireNotNullArgument(alias, "alias");
    }

    public Yield1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        this(operand, ABSENT_ALIAS, false);
    }

    public Yield2 transform(final TransformationContextFromStage1To2 context) {
        return new Yield2(operand.transform(context), alias, hasNonnullableHint);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("alias", alias)
                .add("hasNonnullableHint", hasNonnullableHint)
                .add("operand", operand)
                .$();
    }

}
