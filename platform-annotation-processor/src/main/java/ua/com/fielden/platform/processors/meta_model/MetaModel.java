package ua.com.fielden.platform.processors.meta_model;

public abstract class MetaModel {
    private String context;
    
    public static Class<?> getModelClass() {
        return null;
    }
    
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
