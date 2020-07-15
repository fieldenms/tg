package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.type.TypeResolver;

import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntValue3;

public class EntValue2 implements ISingleOperand2<EntValue3> {
    private final Object value;
    private final boolean ignoreNull;
    private static String yes = "Y";
    private static String no = "N";
    private static TypeResolver tr = new TypeResolver();

    public EntValue2(final Object value) {
        this(value, false);
    }

    public EntValue2(final Object value, final boolean ignoreNull) {
        this.value = preprocessValue(value);
        this.ignoreNull = ignoreNull;
        if (!ignoreNull && value == null) {
            // TODO Uncomment when yieldNull() operator is implemented and all occurences of yield().val(null) are corrected.
            //      throw new IllegalStateException("Value can't be null"); //
        }
    }
    
    private boolean needsParameter() {
        return true;//!(value instanceof Long || value instanceof Integer || value instanceof Short || yes.equals(value) || no.equals(value));
    }

    private Object preprocessValue(final Object value) {
        if (value != null && (value.getClass().isArray() || value instanceof Collection<?>)) {
            final List<Object> values = new ArrayList<Object>();
            for (final Object object : (Iterable) value) {
                final Object furtherPreprocessed = preprocessValue(object);
                if (furtherPreprocessed instanceof List) {
                    values.addAll((List) furtherPreprocessed);
                } else {
                    values.add(furtherPreprocessed);
                }
            }
            return values;
        } else {
            return convertValue(value);
        }
    }

    /** Ensures that values of boolean types are converted properly. */
    private Object convertValue(final Object value) {
        if (value instanceof Boolean) {
            return Boolean.TRUE == value ? "Y" : "N";
        }
        return value;
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
        // TODO EQL
        return value != null ? value.getClass() : null;
    }
    
    @Override
    public Object hibType() {
        // TODO EQL
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
        
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}