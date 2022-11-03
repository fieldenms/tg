package ua.com.fielden.platform.eql.stage1;

public class TransformationResult1<S2> {
    public final S2 item;
    public final TransformationContext1 updatedContext;
    
    public TransformationResult1(final S2 item, final TransformationContext1 updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}
