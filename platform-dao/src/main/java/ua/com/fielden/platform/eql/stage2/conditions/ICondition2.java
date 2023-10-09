package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.eql.stage2.IIgnorableAtStage2;
import ua.com.fielden.platform.eql.stage2.ITransformableToStage3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;

public interface ICondition2<S3 extends ICondition3> extends ITransformableToStage3<S3>, IIgnorableAtStage2 {
}
