package ua.com.fielden.platform.eql.s2.elements;

public interface ISingleOperand2 extends IElement2 {
    /**
     * Determines Java type of the operand. Returns null if type is unknown. If instance is EntProp/EntQuery/EntValue then the type is known, otherwise (Expression/Function) it is
     * unknown.
     *
     * @return
     */
    Class type();
}
