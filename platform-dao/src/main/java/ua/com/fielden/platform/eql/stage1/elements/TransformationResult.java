package ua.com.fielden.platform.eql.stage1.elements;

public class TransformationResult<S2> {
    public final S2 item;
    public final PropsResolutionContext updatedContext;
    
    public TransformationResult(final S2 item, final PropsResolutionContext updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}