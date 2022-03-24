package ua.com.fielden.platform.processors.meta_model;

public abstract class EntityMetaModel {
    private String path;
    
    public EntityMetaModel(String path) {
        this.path = path;
    }
    
    public EntityMetaModel() {
        this("");
    }
    
    public static Class<?> getModelClass() {
        return null;
    }

    protected final String joinPath(String propName) {
        if (this.path.length() > 0) {
            return this.path + "." + propName;
        }
        return propName;
    }
    
    public final String toPath() {
        return this.path;
    }
}
