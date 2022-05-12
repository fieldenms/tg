package ua.com.fielden.platform.processors.metamodel.models;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

public class PropertyMetaModel implements IConvertableToPath {
    private String path;
    
    public PropertyMetaModel(String path) {
        this.path = path;
    }
    
    @Override
    public String toPath() {
        return this.path;
    }
    
    @Override
    public String toString() {
        return toPath();
    }
}
