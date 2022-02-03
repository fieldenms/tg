package ua.com.fielden.platform.entity.meta;

public class MetaModel {
    private String context;
    
    public MetaModel(String context) {
        this.context = context;
    }

    protected final String joinContext(String propName) {
        if (this.context.length() > 0) {
            return this.context + "." + propName;
        }
        return propName;
    }
    
    public final String toString() {
        return this.context;
    }
}
