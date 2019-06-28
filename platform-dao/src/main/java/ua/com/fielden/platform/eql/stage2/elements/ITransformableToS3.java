package ua.com.fielden.platform.eql.stage2.elements;


public interface ITransformableToS3<S3> {
    TransformationResult<S3> transform(TransformationContext context);
}