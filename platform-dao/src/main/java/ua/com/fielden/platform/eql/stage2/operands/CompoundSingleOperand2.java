package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public record CompoundSingleOperand2 (ISingleOperand2<? extends ISingleOperand3> operand,
                                      ArithmeticalOperator operator) {

}
