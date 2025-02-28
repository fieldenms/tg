package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;
import java.util.Set;

abstract class SingleOperandFunction2<T extends ISingleOperand3> extends AbstractFunction2<T> {

    public final ISingleOperand2<? extends ISingleOperand3> operand;

    public SingleOperandFunction2(final ISingleOperand2<? extends ISingleOperand3> operand, final PropType type) {
        super(type);
        this.operand = operand;
    }

    @Override
    public Set<Prop2> collectProps() {
        return operand.collectProps();
    }
    
    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof SingleOperandFunction2 that
                  && Objects.equals(operand, that.operand);
    }

    @Override
    protected ToString addToString(final ToString toString) {
        return super.addToString(toString).add("operand", operand);
    }

}
