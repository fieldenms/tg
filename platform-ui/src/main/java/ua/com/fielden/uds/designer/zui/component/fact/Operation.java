package ua.com.fielden.uds.designer.zui.component.fact;

/**
 * All operations but PREDIACTE are associated with one of the constraint types. PREDICATE operation requires a Java statement, which would evaluate to TRUE or FALSE.
 * 
 * Currently there are only those operations that are supported by JBoss Rules.
 * 
 * @author 01es
 * 
 */
public enum Operation {
    LESS("<"),
    GREATER(">"),
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_EQUAL("<="),
    GREATER_EQUAL(">="),
    CONTAINS("contains"),
    EXCLUDES("excludes"),
    MATCHES("matches"),
    IN("in"),
    PREDICATE("->"); // strictly speaking predicate is not an operation, however for simplicity and without breaking too much of a rule it can be
    // considered as such.

    private String representation;

    private Operation(String representation) {
        setRepresentation(representation);
    }

    public String getRepresentation() {
        return representation;
    }

    private void setRepresentation(String representation) {
        this.representation = representation;
    }

    public boolean equals(Operation op) {
        if (op == null) {
            return false;
        }
        if (op == this) {
            return true;
        }
        return getRepresentation().equals(op.getRepresentation());
    }

    public String toString() {
        return representation;
    }

    public static void main(String[] args) {
        System.out.println(Operation.CONTAINS.equals(Operation.CONTAINS));
        System.out.println(Operation.CONTAINS.equals(Operation.EQUAL));
    }

}
