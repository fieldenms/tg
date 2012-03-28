package ua.com.fielden.uds.designer.zui.component.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IValue;

public class Parenthesis implements IOperator, Serializable {
    private static final long serialVersionUID = -9164591743817103919L;

    private IOperand<IValue> operandValue;
    private IOperand<IValue> operand;

    private OperatorType type;

    @SuppressWarnings("unchecked")
    public Parenthesis() {
	operandValue = new HybridOperand(getDefaultValue().toString());
	operandValue.setOperator(this);
	setType(OperatorType.ANY);
    }

    public Parenthesis(IOperand<IValue> operand) {
	this.operandValue = operand;
    }

    public String getExpression() {
	return "";
    }

    public String getRepresentation() {
	StringBuffer buff = new StringBuffer();
	buff.append("(");
	buff.append(operandValue.getRepresentation());
	buff.append(")");
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
	return "1";
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
	Parenthesis clone = null;
	try {
	    clone = (Parenthesis) super.clone(); // new Parenthesis();
	    // clone.setType(type);
	    clone.operandValue = (IOperand) operandValue.clone();
	    System.out.println("CLONE: " + clone.getClass().getSimpleName() + clone.type);

	} catch (CloneNotSupportedException e) {
	    throw new RuntimeException(e);
	}

	return clone;
    }

    public OperatorType type() {
	return type;
    }

    public void setType(OperatorType type) {
	this.type = type;
    }
}
