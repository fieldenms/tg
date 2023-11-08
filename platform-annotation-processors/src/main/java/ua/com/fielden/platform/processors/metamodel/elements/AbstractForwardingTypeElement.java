package ua.com.fielden.platform.processors.metamodel.elements;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * A base type that implements {@link TypeElement}, which is a subtype of {@link Element}.
 * 
 * @author TG Team
 *
 */
abstract class AbstractForwardingTypeElement extends AbstractForwardingElement<TypeElement> implements TypeElement {

    protected AbstractForwardingTypeElement(final TypeElement element) {
        super(element);
    }

    @Override
    public NestingKind getNestingKind() {
        return element.getNestingKind();
    }

    @Override
    public Name getQualifiedName() {
        return element.getQualifiedName();
    }

    @Override
    public TypeMirror getSuperclass() {
        return element.getSuperclass();
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
        return element.getInterfaces();
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return element.getTypeParameters();
    }

}
