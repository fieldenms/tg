package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.EntValue2;

public class EntValue1 implements ISingleOperand1<EntValue2> {
    private final Object value;
    private final boolean ignoreNull;

    public EntValue1(final Object value) {
        this(value, false);
    }

    public EntValue1(final Object value, final boolean ignoreNull) {
        this.value = value;
        this.ignoreNull = ignoreNull;
        if (!ignoreNull && value == null) {
            // TODO Uncomment when yieldNull() operator is implemented and all occurences of yield().val(null) are corrected.
            //	    throw new IllegalStateException("Value can't be null"); //
        }
    }

    @Override
    public TransformationResult<EntValue2> transform(final PropsResolutionContext resolutionContext) {
        final EntValue2 transformed = new EntValue2(getValue(), isIgnoreNull());
        return new TransformationResult<EntValue2>(transformed, resolutionContext.cloneWithAdded(transformed));
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EntValue1)) {
            return false;
        }
        final EntValue1 other = (EntValue1) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public Object getValue() {
        return value;
    }

    public boolean isIgnoreNull() {
        return ignoreNull;
    }
}