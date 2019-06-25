package ua.com.fielden.platform.eql.stage2.elements.operands;

import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;
import ua.com.fielden.platform.eql.stage2.elements.ITransformableToS3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public interface ISingleOperand2<S3 extends ISingleOperand3> extends IIgnorableAtS2,  ITransformableToS3<S3> {
    /**
     * Determines Java type of the operand. Returns null if type is unknown. If instance is EntProp/EntQuery/EntValue then the type is known, otherwise (Expression/Function) it is
     * unknown.
     * 
     * @return
     */
    Class<?> type();
}
