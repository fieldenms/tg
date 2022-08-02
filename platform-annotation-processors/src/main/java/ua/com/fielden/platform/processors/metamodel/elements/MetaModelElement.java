package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;

/**
 * Represents a source code of a meta-model for a corresponding domain entity.
 *
 * @author TG Team 
 */
public class MetaModelElement extends ForwardingTypeElement {
    private final String packageName;
    
    public MetaModelElement(final TypeElement typeElement, final String packageName) {
        super(typeElement);
        this.packageName = packageName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public ClassName getMetaModelClassName() {
        return ClassName.get(packageName, getSimpleName().toString());
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(getQualifiedName());
    }

}