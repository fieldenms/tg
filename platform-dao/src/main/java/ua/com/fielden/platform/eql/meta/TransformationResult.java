package ua.com.fielden.platform.eql.meta;

public class TransformationResult<S2> {
    private final S2 item;
    private final PropsResolutionContext updatedContext;
    
    public TransformationResult(S2 item, PropsResolutionContext updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }

    public S2 getItem() {
        return item;
    }

    public PropsResolutionContext getUpdatedContext() {
        return updatedContext;
    }
}