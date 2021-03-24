package ua.com.fielden.platform.eql.stage1;

public class TransformationResult<S2> {
    public final S2 item;
    public final TransformationContext updatedContext;
    
    public TransformationResult(final S2 item, final TransformationContext updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}
