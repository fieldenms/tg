package ua.com.fielden.platform.processors.meta_model;

import ua.com.fielden.platform.entity.AbstractEntity;

public abstract class EntityMetaModel {
    private String path;
    
    public EntityMetaModel(String path) {
        this.path = path;
    }
    
    public EntityMetaModel() {
        this("");
    }
    
    public static Class<? extends AbstractEntity> getModelClass() {
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
