package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.Objects;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.ClassName;

import ua.com.fielden.platform.annotations.metamodel.ForType;

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

    /**
     * Returns a {@link TypeMirror} for the underlying entity by looking at {@link ForType} annotation.
     * <p>
     * Note that if the underlying entity no longer exists, then {@link ErrorType} with kind {@link TypeKind.ERROR} is returned.
     * @return type of the underlying entity
     */
    public TypeMirror getEntityType() {
        // in this case the annotation value containts a Class object
        // the information to load or locate a class is not available, since we are in the realm of source code
        // therefore MirroredTypeException should be caught, which provides TypeMirror of that Class object
        try {
            getAnnotation(ForType.class).value();
        } catch (MirroredTypeException e) {
            final TypeMirror entityTypeMirror = e.getTypeMirror();
            return entityTypeMirror;
        }
        return null;
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