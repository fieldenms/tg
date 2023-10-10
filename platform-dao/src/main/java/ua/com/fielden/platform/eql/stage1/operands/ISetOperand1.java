package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.eql.stage1.ITransformableToStage2;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;

public interface ISetOperand1<T extends ISetOperand2<?>> extends ITransformableToStage2<T> {
}
