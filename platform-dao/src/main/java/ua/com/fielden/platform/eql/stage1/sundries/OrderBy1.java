package ua.com.fielden.platform.eql.stage1.sundries;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBy2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

/// [#operand] and [#yieldName] are mutually exclusive: exactly one of them will be not null.
///
public record OrderBy1 (@Nullable ISingleOperand1<? extends ISingleOperand2<?>> operand,
                        @Nullable String yieldName,
                        boolean isDesc)
    implements ToString.IFormattable
{

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean isDesc) {
        this(operand, null, isDesc);
    }

    public OrderBy1(final String yieldName, final boolean isDesc) {
        this(null, yieldName, isDesc);
    }

    public OrderBy2 transform(final TransformationContextFromStage1To2 context) {
        return operand != null ? new OrderBy2(operand.transform(context), isDesc) : new OrderBy2(yieldName, isDesc);
    }

    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand == null ? ImmutableSet.of() : operand.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("yieldName", yieldName)
                .add("isDesc", isDesc)
                .add("operand", operand)
                .$();
    }

}
