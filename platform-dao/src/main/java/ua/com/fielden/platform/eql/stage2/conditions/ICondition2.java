package ua.com.fielden.platform.eql.stage2.conditions;

import ua.com.fielden.platform.eql.stage2.IIgnorableAtS2;
import ua.com.fielden.platform.eql.stage2.ITransformableToS3;
import ua.com.fielden.platform.eql.stage3.conditions.ICondition3;

public interface ICondition2<S3 extends ICondition3> extends ITransformableToS3<S3>, IIgnorableAtS2 {
}
