package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.eql.stage2.IIgnorableAtStage2;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;

public interface ICondition2<T extends ICondition3> extends ITransformableFromStage2To3<T>, IIgnorableAtStage2 {
}
