package ua.com.fielden.uds.designer.zui.component.expression;

public class Division extends AbstractOperator {
    private static final long serialVersionUID = 5781025310259992588L;

    public Division() {
    }

    public String getExpression() {
        return "/";
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

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        IOperator divOperator = new Division();
        IOperand operand = divOperator.getOperands().get(0);
        operand.setValue("100");
        operand = divOperator.getOperands().get(1);
        operand.setValue("20");

        System.out.println(divOperator.getRepresentation());

        divOperator.append(new HybridOperand("12"));

        System.out.println(divOperator.getRepresentation());

        IOperator minusOperator = new Minus();
        operand = minusOperator.getOperands().get(0);
        operand.setValue("100");
        operand = minusOperator.getOperands().get(1);
        operand.setValue("20");

        divOperator.append(new HybridOperand(new Abs(new HybridOperand(minusOperator))));

        System.out.println(divOperator.getRepresentation());
    }

}
