package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;

/**
 * A convenient wrapper around {@link TypeElement} to represent an entity element.
 *
 * @author TG Team
 *
 */
public final class EntityElement extends AbstractForwardingTypeElement {
    private String packageName;

    /**
     * Creates an {@link EntityElement} without a package name. Use at your own risk.
     * 
     * @param typeElement
     * @return {@link EntityElement}
     */
    public static EntityElement wrapperFor(final TypeElement typeElement) {
        return new EntityElement(typeElement, null);
    }

    public EntityElement(final TypeElement typeElement, final String packageName) {
        super(typeElement);
        this.packageName = packageName;
    }
    
    public String getPackageName() {
        return packageName;
    }

    public ClassName getEntityClassName() {
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
        if (!(obj instanceof EntityElement)) {
            return false;
        }
        final EntityElement that = (EntityElement) obj;
        return Objects.equals(this.getQualifiedName(), that.getQualifiedName());
    }

}