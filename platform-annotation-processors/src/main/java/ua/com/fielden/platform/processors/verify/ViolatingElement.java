package ua.com.fielden.platform.processors.verify;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;

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
public record ViolatingElement(
        Element element, Kind kind, String message,
        Optional<AnnotationMirror> annotationMirror, Optional<AnnotationValue> annotationValue,
        List<ViolatingElement> subElements) {

    public ViolatingElement {
        Objects.requireNonNull(element, "Argument [element] cannot be null.");
        Objects.requireNonNull(kind, "Argument [kind] cannot be null.");
        Objects.requireNonNull(message, "Argument [message] cannot be null.");
        Objects.requireNonNull(annotationMirror, "Argument [annotationMirror] cannot be null.");
        Objects.requireNonNull(annotationValue, "Argument [annotationValue] cannot be null.");
        Objects.requireNonNull(subElements, "Argument [subElements] cannot be null.");
    }

    public ViolatingElement(final Element element, final Kind kind, final String message) {
        this(element, kind, message, empty(), empty(), emptyList());
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, List<ViolatingElement> subElements) {
        this(element, kind, message, empty(), empty(), subElements);
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror) {
        this(element, kind, message, of(annotationMirror), empty(), emptyList());
    }

    public ViolatingElement(final Element element, final Kind kind, final String message, final AnnotationMirror annotationMirror, final AnnotationValue annotationValue) {
        this(element, kind, message, of(annotationMirror), of(annotationValue), emptyList());
    }

    public List<ViolatingElement> subElements() {
        return unmodifiableList(this.subElements);
    }

    /**
     * Prints a message using the information stored by this instance.
     * @param messager
     */
    public void printMessage(final Messager messager) {
        if (annotationMirror.isEmpty()) { /* simplest form of message that is present directly on the element */
            messager.printMessage(kind, message, element);
        } else if (annotationValue.isEmpty()) { /* message present on the element's annotation */
            messager.printMessage(kind, message, element, annotationMirror.get());
        } else { /* complete message form - present on the element annotation's element value */
            messager.printMessage(kind, message, element, annotationMirror.get(), annotationValue.get());
        }

        subElements.forEach(elt -> elt.printMessage(messager));
    }

}