package ua.com.fielden.platform.processors.verify;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

/**
 * Represents an element that did not pass verification by some {@link IVerifier}.
 * This class is tailored for convenient use with {@link javax.annotation.processing.Messager}.
 *
 * @author TG Team
 */
public record ViolatingElement(Element element, Kind kind, String message, Optional<AnnotationMirror> annotationMirror, Optional<AnnotationValue> annotationValue) {

    public ViolatingElement {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");
        Objects.requireNonNull(annotationMirror, "Argument [annotationMirror] cannot be null.");
        Objects.requireNonNull(annotationValue, "Argument [annotationValue] cannot be null.");
    }

    public ViolatingElement(final Element element, final Kind kind, final String message) {
        this(element, kind, message, empty(), empty());
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror) {
        this(element, kind, message, of(annotationMirror), empty());
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
        this(element, kind, message, of(annotationMirror), of(annotationValue));
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
            messager.printMessage(kind, message, element, annotationMirror.orElse(null));
        } else {
            // complete message form - present on the element annotation's element value
            messager.printMessage(kind, message, element, annotationMirror.orElse(null), annotationValue.orElse(null));
        }
    }

}