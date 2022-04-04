package ua.com.fielden.platform.processors.meta_model;

import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class EntityElement {

    private TypeElement typeElement;
    private String packageName;

    public EntityElement(TypeElement typeElement, Elements elementUtils) {
        this.typeElement = typeElement;
        this.packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
    
    public String getSimpleName() {
        return typeElement.getSimpleName().toString();
    }
    
    public String getPackageName() {
        return packageName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(this.packageName, getSimpleName());
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
        EntityElement other = (EntityElement) obj;
        return this.packageName.equals(other.getPackageName()) &&
                getSimpleName().equals(other.getSimpleName());
    }
}