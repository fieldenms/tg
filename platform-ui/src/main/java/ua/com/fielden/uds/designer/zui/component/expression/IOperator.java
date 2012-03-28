package ua.com.fielden.uds.designer.zui.component.expression;

import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IValue;

/**
 * A contract for all operators like minus, multiplication etc. An operator may contain many operands. If a two-argument operator like "/" contains more than two operands then it
 * is represented as a set of operands separated by operator's representation. For example, in case of three operands it would be "(operand1) / (operand2) / (operand3)".
 * 
 * @author 01es
 * 
 */
public interface IOperator extends Cloneable {
    /**
     * Returns "-" or "/"...depending on what mathematical operator is represented.
     * 
     * @return
     */
    String getRepresentation();

    /**
     * Return full expression, which includes operands
     * 
     * @return
     */
    String getExpression();

    /**
     * Appends an operand at the right end.
     * 
     * @param operand
     */
    void append(IOperand<IValue> operand);

    /**
     * Removes the last operand.
     * 
     * @return
     */
    IOperand<IValue> removeLast();

    /**
     * Removes a passed operand from the list of operands.
     * 
     * @param operand
     *                item to be removed
     * @return true is removed successfully
     */
    boolean remove(IOperand<IValue> operand);

    /**
     * Insert an operand at position index.
     * 
     * @param index
     *                position where operand should be inserted
     * @param operand
     *                item to be inserted
     * @return
     */
    void insert(int index, IOperand<IValue> operand);

    /**
     * Provides a list of existing operands. Most likely the returned list would not be modifiable.
     * 
     * @return
     */
    List<IOperand<IValue>> getOperands();

    /**
     * @return true if this instance is an operand.
     */
    boolean isOperand();

    /**
     * Allows setting a containing operand if this instance of an operator is an operand.
     */
    void setContainingOperand(IOperand<IValue> operand);

    /**
     * @return a containing operand if this operator is a value for an operand.
     */
    IOperand<IValue> getContainingOperand();

    /**
     * This is a default value for an operand, which fulfills the role of empty operand value.
     * 
     * @return
     */
    Object getDefaultValue();

    /**
     * Compares two operators by their content. Most likely it would use their string representation.
     * 
     * @param <T>
     * @param operator
     * @return
     */
    <T extends IOperator> boolean equalsByContent(T operator);

    /**
     * This method is required for proper cloning of operators.
     * 
     * @return
     */
    Object clone();

    /**
     * Returns operator's type.
     */
    OperatorType type();

    /**
     * Sets operator's type.
     * 
     * @param type
     */
    void setType(OperatorType type);
}
