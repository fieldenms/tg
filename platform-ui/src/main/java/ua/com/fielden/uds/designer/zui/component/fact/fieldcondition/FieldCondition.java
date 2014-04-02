package ua.com.fielden.uds.designer.zui.component.fact.fieldcondition;

import ua.com.fielden.uds.designer.zui.component.fact.ConstraintType;
import ua.com.fielden.uds.designer.zui.component.fact.FactNode;
import ua.com.fielden.uds.designer.zui.component.fact.Operation;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;

/**
 * This class represents a filed condition, which forms part of a fact. It has double nature: 1. Display mode: FieldCondition is embedded into a FactNode; a simple rectangular area
 * with instance's string (toString()) representation is displayed; 2. Design mode: FieldCondition is represented outside of FactNode; provides facility to modify field condition
 * compounds.
 * 
 * The change between modes is dynamic.
 * 
 * Note: most of AnstractNode descendants are composed upon instantiation and displayed when added to an already visible node (e.g. layer). FieldConditionNode has a different
 * behaviour because it changes dynamically and its compounds may be defined or changed at any moment during an application life cycle. It is actually not a descendant from
 * AbstractNode, but rather a factory of AbstractNodes descendants.
 * 
 * @author 01es
 * 
 */
public class FieldCondition {
    private String fieldBinding;
    private String fieldName;
    private Operation operation;
    private ConstraintType constraint;
    private String constraintExpression; // if Operation != PREDICATE then operationExpression needs to be specified
    private String predicateExpression; // if Operation == PREDICATE then predicateExpression needs to be specified instead of a constraintExpression

    private FactNode parent;

    private DesignMode designModeNode;
    private DisplayMode displayModeNode;

    public FieldCondition(FactNode parent) {
        setParent(parent);
    }

    /**
     * This method returns a visual representation of FieldCondition.
     * 
     * @param mode
     * @return
     */
    public AbstractNode visualise(Mode mode) {
        // NOTE: the recreation of visual nodes in this method are required in order to get the most up-to-date representation of FieldCondition.
        // this is due to the fact that not only the design mode can be used for condition modification, but also direct invokation of getters and setters.
        switch (mode) {
        case DISPLAY:
            setDisplayModeNode(new DisplayMode(this));
            return getDisplayModeNode();
        case DESIGN:
            setDesignModeNode(new DesignMode(this, getDisplayModeNode().getExpressionNode()));
            return getDesignModeNode();
        }

        return null;
    }

    public String toString() {
        StringBuffer strBuffer = new StringBuffer();
        if (!"".equals(getFieldBinding()) && getFieldBinding() != null) {
            strBuffer.append(getFieldBinding() + ": ");
        }
        strBuffer.append(toStringWithoutBinding());
        return strBuffer.toString();
    }

    public String toStringWithoutBinding() {
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(getFieldName());
        if (!"".equals(getOperation()) && getOperation() != null) {
            strBuffer.append(" " + getOperation());

            if (Operation.PREDICATE == getOperation()) {
                strBuffer.append(" " + getPredicateExpression());
            } else {
                strBuffer.append(" " + getConstraintExpression());
            }
        }

        return strBuffer.toString();
    }

    public boolean equals(Object obj) {
        System.out.println("equals");
        if (obj == this)
            return true;
        if (!(obj instanceof FieldCondition))
            return false;

        FieldCondition cmpTo = (FieldCondition) obj;

        boolean result = true;
        result &= (getFieldBinding() != null ? getFieldBinding().equals(cmpTo.getFieldBinding()) : getFieldBinding() == cmpTo.getFieldBinding());
        result &= (getFieldName() != null ? getFieldName().equals(cmpTo.getFieldName()) : getFieldName() == cmpTo.getFieldName());
        result &= (getConstraintExpression() != null ? getConstraintExpression().equals(cmpTo.getConstraintExpression())
                : getConstraintExpression() == cmpTo.getConstraintExpression());
        result &= (getPredicateExpression() != null ? getPredicateExpression().equals(cmpTo.getPredicateExpression()) : getPredicateExpression() == cmpTo.getPredicateExpression());
        result &= (getOperation() == cmpTo.getOperation());
        result &= (getConstraint() == cmpTo.getConstraint());

        return result;
    }

    /*
     * public int hashCode() { int result = 0; result += (getFieldBinding() != null ? getFieldBinding().hashCode() * 29: 0); result += (getFieldName() != null ?
     * getFieldName().hashCode() * 29 : 0); result += (getConstraintExpression() != null ? getConstraintExpression().hashCode() * 29 : 0); result +=
     * (getPredicateExpression() != null ? getPredicateExpression().hashCode() * 29 : 0); result += (getOperation() != null ? getOperation().hashCode() * 29 :
     * 0); result += (getConstraint() != null ? getConstraint().hashCode() * 29 : 0); return result; }
     */

    public ConstraintType getConstraint() {
        return constraint;
    }

    public void setConstraint(ConstraintType constraint) {
        this.constraint = constraint;
    }

    public String getConstraintExpression() {
        return constraintExpression;
    }

    public void setConstraintExpression(String constraintExpression) {
        this.constraintExpression = constraintExpression;
    }

    public String getFieldBinding() {
        return fieldBinding;
    }

    public void setFieldBinding(String fieldBinding) {
        this.fieldBinding = fieldBinding;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getPredicateExpression() {
        return predicateExpression;
    }

    public void setPredicateExpression(String predicateExpression) {
        this.predicateExpression = predicateExpression;
    }

    public static enum Mode {
        DISPLAY, DESIGN;
    }

    public DesignMode getDesignModeNode() {
        return designModeNode;
    }

    private void setDesignModeNode(DesignMode designModeNode) {
        this.designModeNode = designModeNode;
    }

    public DisplayMode getDisplayModeNode() {
        return displayModeNode;
    }

    private void setDisplayModeNode(DisplayMode displayModeNode) {
        this.displayModeNode = displayModeNode;
    }

    public FactNode getParent() {
        return parent;
    }

    public void setParent(FactNode parent) {
        this.parent = parent;
    }
}