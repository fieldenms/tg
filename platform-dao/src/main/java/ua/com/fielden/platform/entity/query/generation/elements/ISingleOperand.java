package ua.com.fielden.platform.entity.query.generation.elements;




public interface ISingleOperand extends IPropertyCollector {
    /**
     * Indicates that operand should not be taken into account during model construction (this is used within implementation of ignore of conditions where one/both operands are values with null as value.
     * @return
     */
    boolean ignore();

    /**
     * Determines Java type of the operand. Returns null if type is unknown. If instance is EntProp/EntQuery/EntValue then the type is known, otherwise (Expression/Function) it is unknown.
     * @return
     */
    Class type();

    String sql();
}
