package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.utils.ToString;

public abstract class AbstractSingleOperand3 implements ISingleOperand3, ToString.IFormattable {
    public final PropType type;
    
    public AbstractSingleOperand3(final PropType type) {
        this.type = type;
    }
    
    @Override
    public PropType type() {
        return type;
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("type", type)
                .pipe(this::addToString)
                .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}
