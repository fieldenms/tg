package ua.com.fielden.uds.designer.zui.component.expression;

public class Pow extends AbstractOperator {
    private static final long serialVersionUID = -2935424106859039454L;

    public Pow() {
    }

    public String getExpression() {
	return "^";
    }

    public Object getDefaultValue() {
	return "1";
    }

    protected int getMinNumberOfOperands() {
	return 2;
    }

    protected int getMaxNumberOfOperands() {
	return 2;
    }

}
