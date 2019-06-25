package ua.com.fielden.platform.eql.stage2.elements.conditions;

import ua.com.fielden.platform.eql.stage2.elements.IIgnorableAtS2;
import ua.com.fielden.platform.eql.stage2.elements.ITransformableToS3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.ICondition3;

public interface ICondition2<S3 extends ICondition3> extends ITransformableToS3<S3>, IIgnorableAtS2 {
}
