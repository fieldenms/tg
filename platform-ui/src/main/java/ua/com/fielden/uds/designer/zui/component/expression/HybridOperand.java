package ua.com.fielden.uds.designer.zui.component.expression;

import java.io.Serializable;

import ua.com.fielden.uds.designer.zui.component.generic.value.StringValue;
import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;

/**
 * 
 * This is a hybrid operand, which may have its value of type String or IOperator. Type String should be used for an atom (i.e. the one not containing other operands and operators)
 * operand. Type IOperator should be used for a composite operand
 * 
 * @author 01es
 * 
 */
public class HybridOperand implements IOperand, Serializable {
    private static final long serialVersionUID = 5810416488863327456L;
    private IValue stringValue;
    private IOperator operatorValue;

    private IOperator operator;

    public HybridOperand(String stringValue) {
        setValue(new StringValue(stringValue));
    }

    public HybridOperand(IOperator operatorValue) {
        setOperatorValue(operatorValue);
    }

    public Object getValue() {
        return operatorValue != null ? operatorValue : stringValue;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if (value instanceof IOperator) {
            setOperatorValue((IOperator) value);
        } else if (value instanceof IValue) {
            stringValue = (IValue) value;
            operatorValue = null;
        } else if (value instanceof String) {
            if (stringValue == null) {
                stringValue = new StringValue((String) value);
            } else {
                stringValue.setValue((String) value);
            }
            operatorValue = null;
        } else if (value == null) {
            setOperatorValue(null);
            stringValue = null;
        } else {
            throw new IllegalArgumentException("Unexpected type");
        }
    }

    @SuppressWarnings("unchecked")
    private void setOperatorValue(IOperator operator) {
        operatorValue = operator;
        if (operatorValue != null) {
            operatorValue.setContainingOperand(this);
        }
        stringValue = null;
    }

    public String getRepresentation() {
        return stringValue != null ? stringValue.getValue().toString() : operatorValue.getRepresentation();
    }

    public void erase() {
        setValue(null);
    }

    public IOperator getOperator() {
        return operator;
    }

    public void setOperator(IOperator operator) {
        this.operator = operator;
    }

    public Object clone() {
        HybridOperand clone = null;
        try {
            clone = (HybridOperand) super.clone();
            clone.setValue(null);
            if (operatorValue != null) {
                clone.setValue(operatorValue.clone());
            } else {
                clone.setValue(stringValue.clone());
            }
            // System.out.println(clone.getValue());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings("unchecked")
    public void registerUpdater(IUpdater updater) {
        if (getValue() instanceof IValue) {
            ((IValue) getValue()).registerUpdater(updater);
        }
    }

    @SuppressWarnings("unchecked")
    public void removeUpdater(IUpdater updater) {
        if (getValue() instanceof IValue) {
            ((IValue) getValue()).removeUpdater(updater);
        }
    }

    public String getDefaultValue() {
        return stringValue.getDefaultValue();
    }

    public boolean isEmptyPermitted() {
        return stringValue.isEmptyPermitted();
    }

    public void setDefaultValue(String defaultValue) {
        stringValue.setDefaultValue(defaultValue);
    }

    public void setEmptyPermitted(boolean emptyPermitted) {
        stringValue.setEmptyPermitted(emptyPermitted);
    }
}
