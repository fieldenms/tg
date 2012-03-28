package ua.com.fielden.uds.designer.zui.component.expression;

public class Or extends AbstractOperator {
    // TODO need to implement a restriction, so that only boolean operands are accepted
    private static final long serialVersionUID = -2935424106859039454L;

    public Or() {
    }

    public String getExpression() {
	return "OR";
    }

    public Object getDefaultValue() {
	return "T";
    }

    public OperatorType type() {
	return OperatorType.BOOLEAN;
    }
}
