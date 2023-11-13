package ua.com.fielden.platform.eql.stage1.conditions;

import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;

public interface ICondition1<T extends ICondition2<?>> extends ITransformableFromStage1To2<T> {
}
