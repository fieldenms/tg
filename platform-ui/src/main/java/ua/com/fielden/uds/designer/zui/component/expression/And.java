package ua.com.fielden.uds.designer.zui.component.expression;

public class And extends AbstractOperator {
    private static final long serialVersionUID = -2935424106859039454L;

    public And() {
    }

    public String getExpression() {
        return "AND";
    }

    public Object getDefaultValue() {
        return "T";
    }

    public OperatorType type() {
        return OperatorType.BOOLEAN;
    }
}
