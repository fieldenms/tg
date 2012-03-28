package ua.com.fielden.uds.designer.zui.component.expression;

public class Minus extends AbstractOperator {
    private static final long serialVersionUID = -2935424106859039454L;

    public Minus() {
    }

    public String getExpression() {
	return "-";
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
	IOperator operator = new Minus();
	IOperand operand = operator.getOperands().get(0);
	operand.setValue("100");
	operand = (HybridOperand) operator.getOperands().get(1);
	operand.setValue("20");

	System.out.println(operator.getRepresentation());

	operator.append(new HybridOperand("12"));

	System.out.println(operator.getRepresentation());
    }

    public Object getDefaultValue() {
	return "0";
    }

}
