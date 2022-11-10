package ua.com.fielden.platform.eql.stage2;

import java.util.Set;

import ua.com.fielden.platform.eql.stage2.operands.Prop2;

public interface ITransformableToS3<S3> {
    TransformationResult2<S3> transform(TransformationContext2 context);

    Set<Prop2> collectProps();
}