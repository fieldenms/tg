package ua.com.fielden.platform.processors.metamodel.models;

import jakarta.annotation.Nonnull;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * A class representing a metamodel for an entity property with a type, which does not have its own meta-model.
 * <p>
 * At the moment, it only provides a way to convert context-dependent property reference to a dot-noted string path.
 * However, it has the potential to be enhanced in future to provide access to more property-related information at both design time and runtime.
 *
 * @author TG Team
 *
 */
public class PropertyMetaModel implements IConvertableToPath {
    private final String path;
    
    public PropertyMetaModel(final String path) {
        this.path = path;
    }
    
    @Override
    public @Nonnull String toPath() {
        return this.path;
    }
    
    @Override
    public @Nonnull String toString() {
        return toPath();
    }

}