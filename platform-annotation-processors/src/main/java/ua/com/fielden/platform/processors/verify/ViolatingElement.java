package ua.com.fielden.platform.processors.verify;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.processors.verify.verifiers.Verifier;

/**
 * Represents an element that did not pass verification by some {@link Verifier}.
 * This class is tailored for convenient use with {@link javax.annotation.processing.Messager}.
 *
 * @author TG Team
 */
public final class ViolatingElement {
    private final Element element;
    private final Kind kind;
    private final String message;
    private final AnnotationMirror annotationMirror;
    private final AnnotationValue annotationValue;

    public ViolatingElement(final Element element, final Kind kind, final String message) {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");
        this.element = element;
        this.kind = kind;
        this.message = message;
        this.annotationMirror = null;
        this.annotationValue = null;
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror) {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");
        Objects.requireNonNull(annotationMirror, "Argument [annotationMirror] cannot be null.");
        this.element = element;
        this.kind = kind;
        this.message = message;
        this.annotationMirror = annotationMirror;
        this.annotationValue = null;
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");
        Objects.requireNonNull(annotationMirror, "Argument [annotationMirror] cannot be null.");
        Objects.requireNonNull(annotationValue, "Argument [annotationValue] cannot be null.");
        this.element = element;
        this.kind = kind;
        this.message = message;
        this.annotationMirror = annotationMirror;
        this.annotationValue = annotationValue;
    }

    public Element getElement() {
        return element;
    }

    public Kind getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }

    public Optional<AnnotationMirror> getAnnotationMirror() {
        return Optional.ofNullable(annotationMirror);
    }

    public Optional<AnnotationValue> getAnnotationValue() {
        return Optional.ofNullable(annotationValue);
    }

    /**
     * Prints a message using the information stored by this instance.
     * @param messager
     */
    public void printMessage(final Messager messager) {
        if (annotationMirror == null) {
            // simplest form of message that is present directly on the element
            messager.printMessage(kind, message, element);
            return;
        }
        if (annotationValue == null) {
            messager.printMessage(kind, message, element, annotationMirror);
        } else {
            // complete message form - present on the element annotation's element value
            messager.printMessage(kind, message, element, annotationMirror, annotationValue);
        }
    }

}
