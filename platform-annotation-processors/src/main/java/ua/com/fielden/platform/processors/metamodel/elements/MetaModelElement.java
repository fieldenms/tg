package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;

/**
 * Represents a source code of a meta-model for a corresponding domain entity.
 *
 * @author TG Team 
 */
public final class MetaModelElement extends AbstractForwardingTypeElement {
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

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetaModelElement)) {
            return false;
        }
        final MetaModelElement that = (MetaModelElement) obj;
        return Objects.equals(this.getQualifiedName(), that.getQualifiedName());
    }

}