package ua.com.fielden.platform.eql.stage3.elements.functions;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class AverageOf3 extends SingleOperandFunction3 {
    private final boolean distinct;
    
    public AverageOf3(final ISingleOperand3 operand, final boolean distinct) {
        super(operand);
        this.distinct = distinct;
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }

}
