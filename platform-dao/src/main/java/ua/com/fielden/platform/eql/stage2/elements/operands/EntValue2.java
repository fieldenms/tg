package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.emptySet;

import java.util.Objects;
import java.util.Set;

import org.hibernate.type.TypeResolver;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntValue3;

public class EntValue2 implements ISingleOperand2<EntValue3> {
    private final Object value;
    private final boolean ignoreNull;
    private static TypeResolver tr = new TypeResolver();

    public EntValue2(final Object value) {
        this(value, false);
    }

    public EntValue2(final Object value, final boolean ignoreNull) {
        this.value = value;
        this.ignoreNull = ignoreNull;
    }
    
    private boolean needsParameter() {
        return true;//!(value instanceof Long || value instanceof Integer || value instanceof Short || yes.equals(value) || no.equals(value));
    }
    
    @Override
    public boolean ignore() {
        return ignoreNull && value == null;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Class<?> type() {
        return value != null ? value.getClass() : null;
    }
    
    @Override
    public Object hibType() {
        return value != null ? tr.basic(type().getName()) : null;
    }
    
    @Override
    public TransformationResult<EntValue3> transform(final TransformationContext context) {
        if (needsParameter()) {
            final EntValue3 transformed = new EntValue3(value, context.getNextParamId());
            return new TransformationResult<EntValue3>(transformed, context.cloneWithParamValue(transformed.getParamName(), transformed.value));
        } else {
            return new TransformationResult<EntValue3>(new EntValue3(value, 0), context);
        }
    }

    @Override
    public Set<EntProp2> collectProps() {
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

        if (!(obj instanceof EntValue2)) {
            return false;
        }

        final EntValue2 other = (EntValue2) obj;
        
        return Objects.equals(value, other.value) && ignoreNull == other.ignoreNull;
    }
}