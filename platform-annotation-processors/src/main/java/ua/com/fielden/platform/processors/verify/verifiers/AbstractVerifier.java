package ua.com.fielden.platform.processors.verify.verifiers;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

/**
 * Abstract base verifier providing common behaviour.
 * 
 * @author TG Team
 */
public abstract class AbstractVerifier implements Verifier {

    protected final ProcessingEnvironment processingEnv;
    protected final Messager messager;
    protected final Types typeUtils;
    protected final ElementFinder elementFinder;
    protected final EntityFinder entityFinder;
    protected final Set<Element> violatingElements;
    
    protected AbstractVerifier(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.violatingElements = new HashSet<>();
    }

    /**
     * Returns an unmodifiable set of elements that did not pass verification.
     */
    public Set<Element> getViolatingElements() {
        return Collections.unmodifiableSet(this.violatingElements);
    }

    protected final void printMessageWithAnnotationHint(
            final Kind kind, final String msg, final Element element, 
            final Class<? extends Annotation> annotationType, final String annotationElementName)
    {
        final Optional<? extends AnnotationMirror> maybeMirror = elementFinder.findAnnotationMirror(element, annotationType);
        if (maybeMirror.isEmpty()) {
            // simplest form of message that is present directly on the element
            messager.printMessage(kind, msg, element);
        }
        else {
            final AnnotationMirror mirror = maybeMirror.get();
            final Optional<AnnotationValue> annotElementValue = elementFinder.findAnnotationValue(mirror, annotationElementName);
            if (annotElementValue.isPresent()) {
                // fullest form of error message present on the element's annotation element value
                messager.printMessage(kind, msg, element, mirror, annotElementValue.get());
            }
            else {
                // useful message for debugging
                messager.printMessage(Kind.OTHER, "ANOMALY: AnnotationValue [%s.%s()] was absent. Element: %s. Annotation: %s."
                        .formatted(mirror.getAnnotationType().asElement().getSimpleName(), annotationElementName, element.getSimpleName(), mirror.toString()));
                // error message present on the element's annotation
                messager.printMessage(kind, msg, element, mirror);
            }
        }
    }

}
