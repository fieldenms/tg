package ua.com.fielden.platform.processors.metamodel.concepts;

import static java.lang.String.format;

import java.util.Objects;

import ua.com.fielden.platform.processors.metamodel.MetaModelConstants;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;

public final class MetaModelConcept {
    private EntityElement entityElement;
    private final String simpleNameSuffix = MetaModelConstants.META_MODEL_NAME_SUFFIX;
    private final String packageNameSuffix = MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX;
    
    public MetaModelConcept(EntityElement entityElement) {
        this.entityElement = entityElement;
    }
    
    public EntityElement getEntityElement() {
        return entityElement;
    }

    public String getSimpleName() {
        return entityElement.getSimpleName() + simpleNameSuffix;
    }

    public String getPackageName() {
        return entityElement.getPackageName() + packageNameSuffix;
    }
    
    public String getQualifiedName() {
        return format("%s.%s", getPackageName(), getSimpleName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(getQualifiedName());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetaModelConcept other = (MetaModelConcept) obj;
        return Objects.equals(getQualifiedName(), other.getQualifiedName());
    }
}
