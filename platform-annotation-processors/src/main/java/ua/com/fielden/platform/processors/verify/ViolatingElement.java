package ua.com.fielden.platform.processors.verify;

import ua.com.fielden.platform.processors.verify.annotation.RelaxVerification;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import java.util.*;

import static java.util.Optional.ofNullable;

/**
 * Represents an element that did not pass verification by some {@link IVerifier}. An instance of this class might also hold a list of
 * sub-elements (more instances of this class), which could be useful during verification of a {@link TypeElement} where additional violations
 * need to be captured (e.g., enclosed {@link VariableElement}s).
 * <p>
 * This class is tailored for convenient use with {@link javax.annotation.processing.Messager}.
 *
 * @author TG Team
 */
public class ViolatingElement {
    private final Element element;
    private final Kind kind;
    /** The relaxed kind of this element if it's annotated with {@link RelaxVerification}, otherwise equal to the original kind. */
    private final Kind relaxedKind;
    private final String message;
    private final Optional<AnnotationMirror> annotationMirror;
    private final Optional<AnnotationValue> annotationValue;
    private final List<ViolatingElement> subElements = new LinkedList<>();

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");

        this.element = element;
        this.kind = kind;
        this.relaxedKind = RelaxVerification.Factory.policyFor(element).map(pol -> pol.relaxedKind(kind)).orElse(kind);
        this.message = message;
        this.annotationMirror = ofNullable(annotationMirror);
        this.annotationValue = ofNullable(annotationValue);
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror) {
        this(element, kind, message, annotationMirror, null);
    }

    public ViolatingElement(final Element element, final Kind kind, final String message) {
        this(element, kind, message, null, null);
    }

    public ViolatingElement addSubElements(final Collection<ViolatingElement> elements) {
        this.subElements.addAll(elements);
        return this;
    }

    public ViolatingElement addSubElements(final ViolatingElement... elements) {
        return addSubElements(List.of(elements));
    }

    public List<ViolatingElement> subElements() {
        return Collections.unmodifiableList(this.subElements);
    }

    public Element element() { return element; }
    public Kind kind() { return kind; }
    public Kind relaxedKind() { return relaxedKind; }
    public String message() { return message; }
    public Optional<AnnotationMirror> annotationMirror() { return annotationMirror; }
    public Optional<AnnotationValue> annotationValue() { return annotationValue; }

    /**
     * Indicates whether this violating element indicates an error (is associated with {@link Diagnostic.Kind#ERROR}).
     * Note that the relaxed kind is considered instead of the original one.
     *
     * @see RelaxVerification
     */
    public boolean hasError() {
        return Kind.ERROR.equals(relaxedKind);
    }

    /**
     * Prints a message using the information stored by this instance.
     * <p>
     * If the underlying element is annotated with {@link SkipVerification}, then nothing is printed.
     *
     * @param messager
     */
    public void printMessage(final Messager messager) {
        if (SkipVerification.Factory.shouldSkipVerification(element)) {
            return;
        }

        // use the relaxed kind for the message
        if (annotationMirror.isEmpty()) { /* simplest form of message that is present directly on the element */
            messager.printMessage(relaxedKind, message, element);
        } else if (annotationValue.isEmpty()) { /* message present on the element's annotation */
            messager.printMessage(relaxedKind, message, element, annotationMirror.get());
        } else { /* complete message form - present on the element annotation's element value */
            messager.printMessage(relaxedKind, message, element, annotationMirror.get(), annotationValue.get());
        }

        subElements.forEach(elt -> elt.printMessage(messager));
    }

}
