package ua.com.fielden.platform.processors.metamodel.elements;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.metamodel.exceptions.ForwardingElementException;

/**
 * A base type that implements {@link Element} and acts as a wrapper around an {@code element} instance, where all the {@link Element} contract calls are forwarded to {@code element}. 
 *
 * @author TG Team
 *
 * @param <E> the implementation of {@link Element} to forward calls to
 */
abstract class AbstractForwardingElement<E extends Element> implements Element {
    protected final E element;
    
    protected AbstractForwardingElement(final E element) {
        if (element == null) {
            throw new ForwardingElementException("Value null for an element is not acceptable.");
        }
        this.element = element;
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
    public <R, P> R accept(final ElementVisitor<R, P> v, P p) {
        return element.accept(v, p);
    }

}