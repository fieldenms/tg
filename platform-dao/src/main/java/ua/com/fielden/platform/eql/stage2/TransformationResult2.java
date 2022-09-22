package ua.com.fielden.platform.eql.stage2;

public class TransformationResult2<S3> {
    public final S3 item;
    public final TransformationContext2 updatedContext;

    public TransformationResult2(final S3 item, final TransformationContext2 updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}