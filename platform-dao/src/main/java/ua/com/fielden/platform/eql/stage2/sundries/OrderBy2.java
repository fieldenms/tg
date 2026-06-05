package ua.com.fielden.platform.eql.stage2.sundries;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.eql.stage3.sundries.Yields3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

/// [#operand] and [#yieldName] are mutually exclusive: exactly one of them will be not null.
///
public record OrderBy2 (@Nullable ISingleOperand2<? extends ISingleOperand3> operand,
                        @Nullable String yieldName,
                        boolean isDesc)
        implements ToString.IFormattable
{

    public OrderBy2(final ISingleOperand2<? extends ISingleOperand3> operand, final boolean isDesc) {
        this(operand, null, isDesc);
    }

    public OrderBy2(final String yieldName, final boolean isDesc) {
        this(null, yieldName, isDesc);
    }

    public TransformationResultFromStage2To3<OrderBy3> transform(final TransformationContextFromStage2To3 context, final Yields3 yields) {
        if (operand != null) {
            final TransformationResultFromStage2To3<? extends ISingleOperand3> operandTr = operand.transform(context);
            return new TransformationResultFromStage2To3<>(new OrderBy3(operandTr.item, isDesc), operandTr.updatedContext);
        } else {
            return new TransformationResultFromStage2To3<>(new OrderBy3(yields.yieldsMap().get(yieldName), isDesc), context);
        }
    }

    public Set<Prop2> collectProps() {
        return operand == null ? ImmutableSet.of() : operand.collectProps();
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
