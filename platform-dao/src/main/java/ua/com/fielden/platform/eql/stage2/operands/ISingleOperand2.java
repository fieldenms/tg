package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.IIgnorableAtStage2;
import ua.com.fielden.platform.eql.stage2.ITransformableToStage3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public interface ISingleOperand2<S3 extends ISingleOperand3> extends IIgnorableAtStage2,  ITransformableToStage3<S3> {

    PropType type();
    
    /**
     * Return true in case that the operand is of Entity type and can't contain null values.
     * 
     * @return
     */
    boolean isNonnullableEntity();
}