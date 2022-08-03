package ua.com.fielden.platform.processors.metamodel.elements;

import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;

/**
 * A convenient wrapper around {@link TypeElement} to represent an entity element.
 *
 * @author TG Team
 *
 */
public class EntityElement extends ForwardingTypeElement {
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

}