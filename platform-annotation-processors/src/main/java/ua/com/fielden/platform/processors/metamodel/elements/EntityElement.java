package ua.com.fielden.platform.processors.metamodel.elements;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Objects;

/**
 * A convenient wrapper around {@link TypeElement} to represent an entity element.
 *
 * @author TG Team
 *
 */
public final class EntityElement extends AbstractForwardingTypeElement implements Comparable<EntityElement> {
    private final String packageName;
    private final ClassName entityClassName;

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
        this.entityClassName = packageName == null ? ClassName.get(typeElement) : ClassName.get(packageName, getSimpleName().toString());
    }

    public String getPackageName() {
        return packageName;
    }

    public ClassName getEntityClassName() {
        return entityClassName;
    }

    public boolean isAbstract() {
       return element.getModifiers().contains(Modifier.ABSTRACT);
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

    @Override
    public String toString() {
        return entityClassName.toString();
    }

    @Override
    public int compareTo(final EntityElement that) {
        return entityClassName.simpleName().compareTo(that.entityClassName.simpleName());
    }
}
