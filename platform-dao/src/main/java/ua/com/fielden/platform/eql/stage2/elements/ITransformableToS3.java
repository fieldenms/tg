package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Set;

import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;

public interface ITransformableToS3<S3> {
    TransformationResult<S3> transform(TransformationContext context);
    Set<EntProp2> collectProps();
}