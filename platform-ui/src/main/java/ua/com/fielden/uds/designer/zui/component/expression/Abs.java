package ua.com.fielden.uds.designer.zui.component.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IValue;

/**
 * Modulus or an absolute value operator, which contains only one operand, which cannot be removed and no more operands can be added.
 * 
 * @author 01es
 * 
 */
public class Abs implements IOperator, Serializable {
    private static final long serialVersionUID = -168520114369575038L;

    private IOperand<IValue> operandValue;
    private IOperand<IValue> operand;

    @SuppressWarnings("unchecked")
    public Abs() {
        operandValue = new HybridOperand(getDefaultValue().toString());
        operandValue.setOperator(this);
    }

    public Abs(IOperand<IValue> operand) {
        this.operandValue = operand;
    }

    public String getExpression() {
        return "abs";
    }

    public String getRepresentation() {
        StringBuffer buff = new StringBuffer();
        buff.append("|");
        String operandRep = operandValue.getRepresentation();
        buff.append(operandRep.substring(1, operandRep.length() - 1)); // this is necessary just to remove unnecessary parenthesis
        buff.append("|");

        return buff.toString();
    }

    public void append(IOperand operand) { /* do nothing */
    }

    public IOperand<IValue> removeLast() {
        return null;
    }

    public List<IOperand<IValue>> getOperands() {
        List<IOperand<IValue>> list = new ArrayList<IOperand<IValue>>();
        list.add(operandValue);
        return Collections.unmodifiableList(list);
    }

    public boolean isOperand() {
        return operand != null;
    }

    public void setContainingOperand(IOperand<IValue> operand) {
        this.operand = operand;
    }

    public IOperand<IValue> getContainingOperand() {
        return operand;
    }

    public Object getDefaultValue() {
        return "-1";
    }

    public void insert(int index, IOperand operand) { /* do nothing */
    }

    public boolean remove(IOperand operand) {
        return false;
    }

    public <T extends IOperator> boolean equalsByContent(T operator) {
        if (operator == this) {
            return true;
        }
        return getRepresentation().equals(operator.getRepresentation());
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        Abs clone = null;
        try {
            clone = (Abs) super.clone();
            clone.operandValue = (IOperand) operandValue.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return clone;
    }

    public OperatorType type() {
        return OperatorType.ARITHMETIC;
    }

    public void setType(OperatorType type) {

    }
}
