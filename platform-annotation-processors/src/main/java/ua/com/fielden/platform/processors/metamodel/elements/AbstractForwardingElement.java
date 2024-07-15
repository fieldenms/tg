package ua.com.fielden.platform.processors.metamodel.elements;

import ua.com.fielden.platform.processors.metamodel.exceptions.ForwardingElementException;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * A base type that implements {@link Element} and acts as a wrapper around an {@code element} instance, where all the {@link Element} contract calls are forwarded to {@code element}. 
 *
 * @author TG Team
 *
 * @param <E> the implementation of {@link Element} to forward calls to
 */
// TODO Do not implement Element, because this polymorphism makes certain errors possible,
// such as passing an instance of a class implementing this abstract class to some API that expects its own internal implementation instead.
// For example, Elements.getPackageOf(Element) will throw an exception, if its argument is not of an internal type that implements Element.
// Therefore, composition should be facilitated instead, e.g. myAbstractForwardingElement.element
public abstract class AbstractForwardingElement<E extends Element> implements Element {
    // TODO make public and remove getter, this field is final anyway
    protected final E element;
    
    protected AbstractForwardingElement(final E element) {
        if (element == null) {
            throw new ForwardingElementException("Value null for an element is not acceptable.");
        }
        this.element = element;
    }
    
    public E element() {
        return element;
    }

    @Override
    public String toString() {
        return element.toString();
    }

    @Override
    public TypeMirror asType() {
        return element.asType();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public Name getSimpleName() {
        return element.getSimpleName();
    }

    @Override
    public Element getEnclosingElement() {
        return element.getEnclosingElement();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return element.getEnclosedElements();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return element.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(final Class<A> annotationType) {
        return element.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(final ElementVisitor<R, P> v, final P p) {
        return element.accept(v, p);
    }

}
