package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.conditions.NullPredicate2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

/**
 * A predicate for SQL's IS NULL / IS NOT NULL statement.
 * Supports operands of union type by checking every member for nullability.
 *
 * @author TG Team
 */
public record NullPredicate1 (ISingleOperand1<? extends ISingleOperand2<?>> operand, boolean negated)
        implements ICondition1<ICondition2<?>>, ToString.IFormattable
{

    @Override
    public ICondition2<?> transform(final TransformationContextFromStage1To2 context) {
        return new NullPredicate2(operand.transform(context), negated);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("negated", negated)
                .add("operand", operand)
                .$();
    }

}
