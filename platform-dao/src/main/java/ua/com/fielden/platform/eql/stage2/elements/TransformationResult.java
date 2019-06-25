package ua.com.fielden.platform.eql.stage2.elements;

public class TransformationResult<S3> {
    private final S3 item;
    private final TransformationContext updatedContext;
    
    public TransformationResult(S3 item, TransformationContext updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }

    public S3 getItem() {
        return item;
    }

    public TransformationContext getUpdatedContext() {
        return updatedContext;
    }
}