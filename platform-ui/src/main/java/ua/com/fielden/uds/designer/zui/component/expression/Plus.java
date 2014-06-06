package ua.com.fielden.uds.designer.zui.component.expression;

public class Plus extends AbstractOperator {
    private static final long serialVersionUID = 5781025310259992588L;

    public Plus() {
    }

    public String getExpression() {
        return "+";
    }

    public Object getDefaultValue() {
        return "0";
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        IOperator plusOperator = new Plus();
        IOperand operand = plusOperator.getOperands().get(0);
        operand.setValue("100");
        operand = plusOperator.getOperands().get(1);
        operand.setValue("20");

        System.out.println(plusOperator.getRepresentation());

        plusOperator.append(new HybridOperand("12"));

        System.out.println(plusOperator.getRepresentation());

        IOperator minusOperator = new Minus();
        operand = minusOperator.getOperands().get(0);
        operand.setValue("100");
        operand = minusOperator.getOperands().get(1);
        operand.setValue("20");

        plusOperator.append(new HybridOperand(new Abs(new HybridOperand(minusOperator))));

        System.out.println(plusOperator.getRepresentation());
    }

}
