package ua.com.fielden.platform.eql.stage2.operands.functions;

import java.util.Set;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.operands.AbstractSingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public abstract class AbstractFunction2<S3 extends ISingleOperand3> extends AbstractSingleOperand2 implements ISingleOperand2<S3> {

    public AbstractFunction2(PropType type) {
        super(type);
    }
    
    public AbstractFunction2(final Set<PropType> types) {
        super(types);
    }

    @Override
    public boolean ignore() {
        return false;
    }
    
    @Override
    public boolean isNonnullableEntity() {
        return false;
    }
}