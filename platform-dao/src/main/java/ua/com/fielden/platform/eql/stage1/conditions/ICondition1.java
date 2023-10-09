package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.eql.stage1.ITransformableToStage2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;

public interface ICondition1<S2 extends ICondition2<?>> extends ITransformableToStage2<S2> {
}
