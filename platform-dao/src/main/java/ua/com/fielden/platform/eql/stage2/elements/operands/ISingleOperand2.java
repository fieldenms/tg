package ua.com.fielden.platform.eql.stage2.elements.operands;

import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;

public interface ISingleOperand2 extends IIgnorableAtS2 {
    /**
     * Determines Java type of the operand. Returns null if type is unknown. If instance is EntProp/EntQuery/EntValue then the type is known, otherwise (Expression/Function) it is
     * unknown.
     * 
     * @return
     */
    Class<?> type();
}
