package ua.com.fielden.platform.eql.stage2.elements;

public class TransformationResult<S3> {
    public final S3 item;
    public final TransformationContext updatedContext;
    
    public TransformationResult(final S3 item, final TransformationContext updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}