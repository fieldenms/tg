package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Value2;

import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;

public class Value1 implements ISingleOperand1<Value2> {
    public static final Value1 NULL = new Value1(null, false);
    public static final Value1 INULL = new Value1(null, true);

    public final Object value;
    public final boolean ignoreNull;

    private Value1(final Object value) {
        this(value, false);
    }

    private Value1(final Object value, final boolean ignoreNull) {
        this.value = value;
        this.ignoreNull = ignoreNull;
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
        return new Value2(value, ignoreNull);
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ignoreNull ? 1231 : 1237);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Value1)) {
            return false;
        }

        final Value1 other = (Value1) obj;

        return Objects.equals(value, other.value) && ignoreNull == other.ignoreNull;
    }
}
