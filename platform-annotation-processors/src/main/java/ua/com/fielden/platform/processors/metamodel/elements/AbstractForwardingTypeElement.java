package ua.com.fielden.platform.processors.metamodel.elements;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

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