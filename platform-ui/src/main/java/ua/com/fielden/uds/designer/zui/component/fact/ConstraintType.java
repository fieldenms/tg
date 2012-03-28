package ua.com.fielden.uds.designer.zui.component.fact;

/**
 * Types of constraints supported by JBoss Rules.
 * 
 * @author 01es
 * 
 */
public enum ConstraintType {
    LITERAL("literalConstraint"), BOUND_VARIABLE_CONSTRAINT("boundVariableConstraint"), RETURN_VALUE_CONSTRAINT("returnValueConstraint");

    private String representation;

    private ConstraintType(String representation) {
	setRepresentation(representation);
    }

    private String getRepresentation() {
	return representation;
    }

    private void setRepresentation(String representation) {
	this.representation = representation;
    }

    public String toString() {
	return getRepresentation();
    }
}