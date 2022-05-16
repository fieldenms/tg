package ua.com.fielden.platform.processors.metamodel.models;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelException;

public abstract class EntityMetaModel implements IConvertableToPath {
    private final String path;
    
    public EntityMetaModel(String path) {
        if (path == null)
            throw new EntityMetaModelException(String.format("%s constructor received null as an argument.", this.getClass().getSimpleName()));
        this.path = path;
    }
    
    public EntityMetaModel() {
        this("");
    }
    
    protected final String joinPath(String propName) {
        if (!this.path.isEmpty())
            return String.format("%s.%s", this.path, propName);

        return propName;
    }
    
    /**
     * Returns the dot-notated path captured in the context. If this entity meta-model has no surrounding context, then this method returns {@code "this"} string constant.
     * <p> 
     * Example:
     * 
     * <pre>
     * public class PersonMetaModel extends EntityMetaModel {
     *      ...
     * }
     * 
     * PersonMetaModel personBase = new PersonMetaModel();
     * personBase.toPath(); // "this"
     * 
     * PersonMetaModel personWithContext = new PersonMetaModel("owner");
     * personWithContext.toPath(); // "owner"
     * </pre>
     */
    @Override
    public final String toPath() {
        if (this.path.isEmpty())
            return "this";

        return this.path;
    }

    @Override
    public final String toString() {
        return toPath();
    }

    /**
     * Returns the underlying entity class of this meta-model.
     * @return
     */
    public abstract Class<? extends AbstractEntity> getEntityClass();
}
