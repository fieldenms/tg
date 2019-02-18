package ua.com.fielden.platform.eql.stage1.elements;

public abstract class AbstractElement1 {
    private final int contextId;
    
    public int getContextId() {
        return contextId;
    }

    protected AbstractElement1(final int contextId) {
        this.contextId = contextId;
    }
}