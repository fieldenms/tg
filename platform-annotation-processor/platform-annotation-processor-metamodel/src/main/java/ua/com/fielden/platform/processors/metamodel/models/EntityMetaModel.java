package ua.com.fielden.platform.processors.metamodel.models;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public abstract class EntityMetaModel implements IConvertableToPath {
    private String path;
    
    public EntityMetaModel(String path) {
        this.path = path;
    }
    
    public EntityMetaModel() {
        this("");
    }
    
    protected final String joinPath(String propName) {
        if (this.path.length() > 0) {
            return this.path + "." + propName;
        }
        return propName;
    }
    
    @Override
    public final String toPath() {
        return this.path;
    }

    @Override
    public final String toString() {
        return toPath();
    }

    public abstract Class<? extends AbstractEntity> getEntityClass();
}
