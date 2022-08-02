package ua.com.fielden.platform.processors.metamodel.elements;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelException;

/**
 * A convenient wrapper around {@link VariableElement} to represent a property element.
 *
 * @author TG Team
 *
 */
public class PropertyElement extends ForwardingVariableElement {
    
    public PropertyElement(final VariableElement varElement) {
        super(varElement);
    }
    
    /**
     * Determines if the property type represent a class or an interfaces ({@link TypeKind#DECLARED}).
     * It is useful to differentiate properties, which are of some entity type with those of ordinary types.
     * However, this predicate returns {@code true} also for {@link BigDecimal}, {@link Integer}, etc.
     *
     * @return
     */
    public boolean hasClassOrInterfaceType() {
        return element.asType().getKind() == TypeKind.DECLARED;
    }

    /**
     * Returns the type of this property.
     * @return
     */
    public TypeMirror getType() {
        return element.asType();
    }

    /**
     * Returns the type of this property as a {@link TypeElement} instance. Throws a runtime exception if the type is not a declared one (refer to {@link TypeKind}).
     * @return
     */
    public TypeElement getTypeAsTypeElement() {
        if (!hasClassOrInterfaceType()) {
            final String message = format("Type of property %s is not a declared type (%s)", getSimpleName(), getType());
            throw new EntityMetaModelException(message);
        }
        return getTypeAsTypeElementOrThrow();
    }

    /**
     * The same as {@link getTypeAsTypeElement} but with unsafe type casting. Use this method only if you are sure that the type of this property is a declared one.
     * @return
     */
    public TypeElement getTypeAsTypeElementOrThrow() {
        return (TypeElement) ((DeclaredType) asType()).asElement();
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(getSimpleName().toString());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PropertyElement)) {
            return false;
        }
        final PropertyElement that = (PropertyElement) obj;
        return Objects.equals(getSimpleName().toString(), that.getSimpleName().toString());
    }

}