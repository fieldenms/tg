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
 * Forwards calls to a given {@link Element} implementation.
 * @author TG Team
 *
 * @param <E> the implementation of {@link Element} to forward calls to
 */
class ForwardingElement<E extends Element> implements Element {
    protected final E element;
    
    /**
     * Creates an implementation of {@link Element} that forwards its calls to <code>element</code>.
     * @param element the element to forward calls to
     */
    protected ForwardingElement(final E element) {
        if (element == null) {
            throw new ForwardingElementException("Constructor received null as an argument.");
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
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return element.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return element.accept(v, p);
    }

}
