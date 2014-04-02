package ua.com.fielden.uds.designer.zui.component.expression;

public class Multiplication extends AbstractOperator {
    private static final long serialVersionUID = 5781025310259992588L;

    public Multiplication() {
    }

    public String getExpression() {
        return "*";
    }

    public Object getDefaultValue() {
        return "1";
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        IOperator mulOperator = new Multiplication();
        IOperand operand = mulOperator.getOperands().get(0);
        operand.setValue("100");
        operand = mulOperator.getOperands().get(1);
        operand.setValue("20");

        System.out.println(mulOperator.getRepresentation());

        mulOperator.append(new HybridOperand("12"));

        System.out.println(mulOperator.getRepresentation());

        IOperator minusOperator = new Minus();
        operand = minusOperator.getOperands().get(0);
        operand.setValue("100");
        operand = minusOperator.getOperands().get(1);
        operand.setValue("20");

        mulOperator.append(new HybridOperand(new Abs(new HybridOperand(minusOperator))));

        System.out.println(mulOperator.getRepresentation());
    }

}
