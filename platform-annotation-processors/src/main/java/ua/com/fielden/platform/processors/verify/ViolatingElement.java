package ua.com.fielden.platform.processors.verify;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.processors.verify.annotation.RelaxVerification;
import ua.com.fielden.platform.processors.verify.annotation.RelaxVerificationFactory;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

/**
 * Represents an element that did not pass verification by some {@link IVerifier}. An instance of this class might also hold a list of
 * sub-elements (more instances of this class), which could be useful during verification of a {@link TypeElement} where additional violations
 * need to be captured (e.g., enclosed {@link VariableElement}s).
 * <p>
 * This class is tailored for convenient use with {@link javax.annotation.processing.Messager}.
 *
 * @author TG Team
 */
public record ViolatingElement (
        Element element,
        Kind kind,
        /** The relaxed kind of this element if it's annotated with {@link RelaxVerification}, otherwise equal to the original kind. */
        Kind relaxedKind,
        String message,
        Optional<AnnotationMirror> annotationMirror,
        Optional<AnnotationValue> annotationValue,
        List<ViolatingElement> subElements)
{

    public ViolatingElement (
            final Element element,
            final Kind kind,
            final Kind relaxedKind,
            final String message,
            final Optional<AnnotationMirror> annotationMirror,
            final Optional<AnnotationValue> annotationValue,
            final List<ViolatingElement> subElements)
    {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");
        this.element = element;
        this.kind = kind;
        this.relaxedKind = RelaxVerificationFactory.policyFor(element).map(pol -> pol.relaxedKind(kind)).orElse(kind);
        this.message = message;
        this.annotationMirror = annotationMirror;
        this.annotationValue = annotationValue;
        this.subElements = List.copyOf(subElements);
    }

    public ViolatingElement(final Element element, final Kind kind, final String message) {
        this(element, kind, null, message, empty(), empty(), emptyList());
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final List<ViolatingElement> subElements) {
        this(element, kind, null, message, empty(), empty(), subElements);
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror) {
        this(element, kind, null, message, of(annotationMirror), empty(), emptyList());
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
        this(element, kind, null, message, of(annotationMirror), of(annotationValue), emptyList());
    }

    public List<ViolatingElement> subElements() {
        return unmodifiableList(this.subElements);
    }

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