package ua.com.fielden.platform.processors.meta_model;

public class PropertyMetaModel {
    private String path;
    
    public PropertyMetaModel(String path) {
        this.path = path;
    }
    
    public String toPath() {
        return this.path;
    }
    
    @Override
    public String toString() {
        return toPath();
    }
}
