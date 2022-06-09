package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.squareup.javapoet.ClassName;

/**
 * Represents a source code of a meta-model for a corresponding domain entity.
 *
 * @author TG Team 
 */
public class MetaModelElement {
    private final TypeElement typeElement;
    private final String simpleName;
    private final String packageName;
    private final String qualifiedName;

    public MetaModelElement(final TypeElement typeElement, final Elements elementUtils) {
        this.typeElement = typeElement;
        this.qualifiedName = typeElement.getQualifiedName().toString();
        this.simpleName = typeElement.getSimpleName().toString();
        this.packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
    
    public String getSimpleName() {
        return simpleName;
    }

    public String getPackageName() {
        return packageName;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }
    
    public ClassName getMetaModelClassName() {
        return ClassName.get(getPackageName(), getSimpleName());
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(qualifiedName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetaModelElement)) {
            return false;
        }
        final MetaModelElement that = (MetaModelElement) obj;
        return Objects.equals(this.qualifiedName, that.qualifiedName);
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }

}