package ua.com.fielden.uds.designer.zui.component.expression;

public class GreaterEq extends AbstractOperator {
    private static final long serialVersionUID = 5781025310259992588L;

    public GreaterEq() {
    }

    public String getExpression() {
        return ">=";
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

    public OperatorType type() {
        return OperatorType.ANY;
    }

}
