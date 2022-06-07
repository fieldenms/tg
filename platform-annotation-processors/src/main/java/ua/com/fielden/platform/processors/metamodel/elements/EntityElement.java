package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * A convenient wrapper around {@link TypeElement} to represent an entity element.
 *
 * @author TG Team
 *
 */
public class EntityElement {
    private final TypeElement typeElement;
    private final String simpleName;
    private final String packageName;
    private final String qualifiedName;

    public EntityElement(final TypeElement typeElement, final Elements elementUtils) {
        this.typeElement = typeElement;
        this.qualifiedName = typeElement.getQualifiedName().toString();
        this.simpleName = typeElement.getSimpleName().toString();
        this.packageName = elementUtils != null ? elementUtils.getPackageOf(typeElement).getQualifiedName().toString() : null;
    }

    private EntityElement(final TypeElement typeElement) {
        this(typeElement, null);
    }

    /**
     * Returns a wrapper for {@link TypeElement} and should only be used for this sole purpose.
     * @return {@link EntityElement}
     */
    public static EntityElement wrapperFor(final TypeElement typeElement) {
        return new EntityElement(typeElement);
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

    public TypeMirror asType() {
        return this.typeElement.asType();
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(qualifiedName);
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
        return Objects.equals(this.qualifiedName, that.qualifiedName);
    }

}