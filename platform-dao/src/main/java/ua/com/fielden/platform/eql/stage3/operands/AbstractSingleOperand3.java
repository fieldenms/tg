package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

public abstract class AbstractSingleOperand3 implements ISingleOperand3 {
    public final PropType type;
    
    public AbstractSingleOperand3(final PropType type) {
        this.type = type;
    }
    
    @Override
    public PropType type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractSingleOperand3 that
                  && Objects.equals(this.type, that.type);
    }

    @Override
    public String toString() {
        return ToString.separateLines.toString(this)
            .add("type", type)
            .pipe(this::addToString)
            .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}
