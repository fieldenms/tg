package ua.com.fielden.platform.processors.meta_model;

import java.util.Objects;

/**
 * A helper class for conversion of package and class names between an entity and its meta-model. 
 */

public class MetaModelElement {
    private EntityElement entityElement;
    private String nameSuffix;
    private String packageSuffix;

    public MetaModelElement(EntityElement EntityElement, String nameSuffix, String packageSuffix) {
        this.entityElement = EntityElement;
        this.nameSuffix = nameSuffix;
        this.packageSuffix = packageSuffix;
    }

    public EntityElement getEntityElement() {
        return entityElement;
    }

    public String getSimpleName() {
        return entityElement.getSimpleName() + nameSuffix;
    }

    public String getPackageName() {
        return entityElement.getPackageName() + packageSuffix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(entityElement);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetaModelElement other = (MetaModelElement) obj;
        return Objects.equals(entityElement, other.entityElement);
    }
}