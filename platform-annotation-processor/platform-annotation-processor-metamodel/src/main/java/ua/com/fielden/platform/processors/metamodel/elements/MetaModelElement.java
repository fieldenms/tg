package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Represents a source code meta-model construct. 
 */

public class MetaModelElement {
    private TypeElement typeElement;
    private PackageElement packageElement;

    public MetaModelElement(TypeElement typeElement, Elements elementUtils) {
        this.typeElement = typeElement;
        this.packageElement = elementUtils.getPackageOf(typeElement);
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
    
    public PackageElement getPackageElement() {
        return this.packageElement;
    }

    public String getSimpleName() {
        return typeElement.getSimpleName().toString();
    }

    public String getPackageName() {
        return packageElement.getQualifiedName().toString();
    }
    
    public String getQualifiedName() {
        return typeElement.getQualifiedName().toString();
    }
    
    public TypeMirror asType() {
        return this.typeElement.asType();
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetaModelElement other = (MetaModelElement) obj;
        return Objects.equals(getQualifiedName(), other.getQualifiedName());
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}