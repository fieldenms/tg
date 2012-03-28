package ua.com.fielden.uds.designer.zui.component.expression;

import ua.com.fielden.uds.designer.zui.interfaces.IValue;

/**
 * This is a contract for the IOperator operands.
 * 
 * @author 01es
 * 
 * @param <T>
 *                Operand's type (e.g. String, Double, IOperator etc.)
 */
public interface IOperand<T> extends IValue<T> {
    /**
     * String representation.
     * 
     * @return
     */
    String getRepresentation();

    /**
     * Erases operand's content;
     */
    void erase();

    /**
     * Returns an operator to which it belongs.
     * 
     * @return
     */
    IOperator getOperator();

    /**
     * Sets an operator to which an instance of operand is attached.
     * 
     * @return
     */
    void setOperator(IOperator operator);
}
