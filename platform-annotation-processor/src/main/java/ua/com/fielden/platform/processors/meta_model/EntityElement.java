package ua.com.fielden.platform.processors.meta_model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    /**
     * Get all properties of this entity.
     * 
     * @param includedInheritedPropertiesNames - names of inherited properties that need to be retrieved
     */
    public Set<PropertyElement> getProperties(List<String> includedInheritedPropertiesNames) {
        final Set<PropertyElement> properties = EntityFinder.findDeclaredProperties(typeElement);
        final List<String> declaredPropertiesNames = properties.stream().map(prop -> prop.getName()).toList();

        final List<PropertyElement> inheritedProperties = EntityFinder.findInheritedProperties(typeElement).stream()
                .filter(prop -> {
                    String propName = prop.getName();
                    return includedInheritedPropertiesNames.contains(propName) &&
                            !(declaredPropertiesNames.contains(propName));
                })
                .toList();
        properties.addAll(inheritedProperties);

        return properties;
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