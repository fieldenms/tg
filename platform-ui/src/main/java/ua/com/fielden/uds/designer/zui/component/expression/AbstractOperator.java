package ua.com.fielden.uds.designer.zui.component.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.IValue;

public abstract class AbstractOperator implements IOperator, Serializable {
    protected List<IOperand<IValue>> operands = new ArrayList<IOperand<IValue>>();
    private IOperand<IValue> operand;

    /**
     * Default constructor, which two default operands.
     */
    @SuppressWarnings("unchecked")
    public AbstractOperator() {
        append(new HybridOperand(getDefaultValue().toString()));
        append(new HybridOperand(getDefaultValue().toString()));
    }

    public void append(IOperand<IValue> operand) {
        if (operands.size() < getMaxNumberOfOperands()) {
            operands.add(operand);
            operand.setOperator(this);
        }
    }

    /**
     * There will always be at least two operands.
     */
    public IOperand<IValue> removeLast() {
        if (operands.size() > getMinNumberOfOperands()) {
            return operands.remove(operands.size() - 1);
        }
        return null;
    }

    /**
     * There will always be at least two operands.
     */
    public boolean remove(IOperand operand) {
        if (operands.size() > getMinNumberOfOperands()) {
            return operands.remove(operand);
        }
        return false;
    }

    public void insert(int index, IOperand<IValue> operand) {
        if (operands.size() < getMaxNumberOfOperands()) {
            operands.add(index, operand);
            operand.setOperator(this);
        }
    }

    public String getRepresentation() {
        StringBuffer buff = new StringBuffer();
        buff.append("(");
        for (int index = 0; index < operands.size() - 1; index++) {
            IOperand operand = operands.get(index);
            buff.append(operand.getRepresentation());
            buff.append(getExpression());
        }
        buff.append(operands.get(operands.size() - 1).getRepresentation());
        buff.append(")");

        return buff.toString();
    }

    public List<IOperand<IValue>> getOperands() {
        return Collections.unmodifiableList(operands);
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

    protected int getMinNumberOfOperands() {
        return 2;
    }

    protected int getMaxNumberOfOperands() {
        return Integer.MAX_VALUE;
    }

    public <T extends IOperator> boolean equalsByContent(T operator) {
        if (operator == this) {
            return true;
        }
        return getRepresentation().equals(operator.getRepresentation());
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        AbstractOperator clone = null;
        try {
            clone = (AbstractOperator) super.clone();
            clone.operands = new ArrayList<IOperand<IValue>>();
            for (IOperand operand : operands) {
                IOperand clonedOp = (IOperand) operand.clone();
                clonedOp.setOperator(clone);
                clone.operands.add(clonedOp);
            }
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
