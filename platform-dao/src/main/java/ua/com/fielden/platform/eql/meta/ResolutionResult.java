package ua.com.fielden.platform.eql.meta;

public class ResolutionResult {
    public final ResolutionContext updatedContext;

    public ResolutionResult(final ResolutionContext updatedContext) {
        this.updatedContext = updatedContext;
    }
    
    public boolean isSuccessful() {
        return updatedContext.pending.isEmpty();
    }
    
    public AbstractPropInfo<?, ?> getResolution() {
        return updatedContext.resolved.get(updatedContext.resolved.size() - 1);
    }
}
