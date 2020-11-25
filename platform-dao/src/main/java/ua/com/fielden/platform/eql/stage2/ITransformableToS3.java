package ua.com.fielden.platform.eql.stage2;

import java.util.Set;

import ua.com.fielden.platform.eql.stage2.operands.EntProp2;

public interface ITransformableToS3<S3> {
    TransformationResult<S3> transform(TransformationContext context);
    Set<EntProp2> collectProps();
}