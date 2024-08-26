package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Value2;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Instead of the constructor, static methods {@link #value()} and {@link #nullValue(boolean)} should be used.
 *
 * @param value  the actual value of this operand (compiles to an SQL literal)
 * @param ignoreNull  indicates whether the expression containing this operand can be ignored if the value is {@code null}
 */
public record Value1(Object value, boolean ignoreNull) implements ISingleOperand1<Value2> {

    public static final Value1 NULL = new Value1(null, false);
    public static final Value1 INULL = new Value1(null, true);

    private Value1(final Object value) {
        this(value, false);
    }

    public static Value1 value(final Object value, final boolean ignoreNull) {
        if (value == null) {
            return ignoreNull ? INULL : NULL;
        }
        return new Value1(value, ignoreNull);
    }

    public static Value1 value(final Object value) {
        return value == null ? NULL : new Value1(value);
    }

    public static Value1 nullValue(final boolean ignoreNull) {
        return ignoreNull ? INULL : NULL;
    }

    @Override
    public Value2 transform(final TransformationContextFromStage1To2 context) {
        return new Value2(value(), ignoreNull());
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }

}
