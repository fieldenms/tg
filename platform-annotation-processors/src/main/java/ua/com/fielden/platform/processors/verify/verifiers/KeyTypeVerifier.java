package ua.com.fielden.platform.processors.verify.verifiers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;

/**
 * Performs verification of a domain model with respect to the {@link KeyType} annotation.
 * <p>
 * The following conditions will cause verification failure:
 * <ol>
 *  <li>Entity definition is missing {@link KeyType} i.e., neither the entity type nor its super types have this annotation declared. Abstract entities should be able have no {@link KeyType} annotation present.</li>
 *  <li>The type of key as defined by {@link KeyType} does not match the one specified as the type argument to {@link AbstractEntity}.</li>
 *  <li>Child entity declares {@link KeyType} that does not match the type at the super type level.</li>
 *  <li>Entity declares property {@code key} having a type that does not match the one defined by {@link KeyType}.</li>
 * </ol> 
 * 
 * @author TG Team
 */
public class KeyTypeVerifier extends AbstractComposableVerifier {

    private static final Class<KeyType> AT_KEY_TYPE_CLASS = KeyType.class;

    private final List<AbstractComposableVerifierPart> verifiers = List.of(
            new KeyTypePresence(),
            new KeyTypeValueMatchesAbstractEntityTypeArgument());

    public KeyTypeVerifier(final ProcessingEnvironment procEnv) {
        super(procEnv);
    }

    public Collection<AbstractComposableVerifierPart> verifierParts() {
        return this.verifiers;
    }

    /**
     * Entity definition must include {@link KeyType} i.e., the entity type itself or its super types must have this annotation declared. 
     * Abstract entities should be able to omit this annotation.
     * 
     * @author TG Team
     */
    private class KeyTypePresence extends AbstractComposableVerifierPart {

        public boolean verify(final RoundEnvironment roundEnv) {
            final List<EntityElement> entitiesMissingKeyType = roundEnv.getRootElements().stream()
                    .filter(el -> elementFinder.isTopLevelClass(el))
                    .map(el -> (TypeElement) el)
                    .filter(el -> entityFinder.isEntityType(el))
                    .map(el -> entityFinder.newEntityElement(el))
                    // include only entity types missing @KeyType (whole type hierarchy is traversed)
                    .filter(el -> entityFinder.findAnnotation(el, AT_KEY_TYPE_CLASS).isEmpty())
                    // skip abstract entity types
                    .filter(el -> !elementFinder.isAbstract(el))
                    .toList();

            entitiesMissingKeyType.forEach(entity -> messager.printMessage(Kind.ERROR, 
                    "Entity definition is missing @%s.".formatted(AT_KEY_TYPE_CLASS.getSimpleName()), entity.element()));
            violatingElements.addAll(entitiesMissingKeyType);

            return entitiesMissingKeyType.isEmpty();
        }
    }

    /**
     * The type of key as defined by {@link KeyType} must match the one specified as the type argument to {@link AbstractEntity}.
     */
    private class KeyTypeValueMatchesAbstractEntityTypeArgument extends AbstractComposableVerifierPart {

        public boolean verify(final RoundEnvironment roundEnv) {
            boolean allPassed = true;
            final List<TypeElement> keyTypeAnnotatedElements = roundEnv.getElementsAnnotatedWith(AT_KEY_TYPE_CLASS).stream()
                    .filter(elementFinder::isTopLevelClass)
                    .map(el -> (TypeElement) el)
                    .toList();

            for (final TypeElement el : keyTypeAnnotatedElements) {
                final TypeMirror keyType = entityFinder.getKeyType(el.getAnnotation(AT_KEY_TYPE_CLASS));

                final DeclaredType parentEntity = (DeclaredType) el.getSuperclass();

                // only if this entity type directly extends AbstractEntity
                if (elementFinder.equals((TypeElement) parentEntity.asElement(), AbstractEntity.class)) {
                    final List<? extends TypeMirror> typeArgs = parentEntity.getTypeArguments();
                    if (typeArgs.isEmpty()) {
                        violatingElements.add(el);
                        allPassed = false;

                        messager.printMessage(Kind.ERROR, 
                                "%s must be parameterized with entity key type.".formatted(AbstractEntity.class.getSimpleName()),
                                el);
                    }
                    else if (!typeUtils.isSameType(typeArgs.get(0), keyType)) {
                        violatingElements.add(el);
                        allPassed = false;

                        // report error
                        printMessageWithAnnotationHint(Kind.ERROR,
                                "Key type must match the type argument to %s.".formatted(AbstractEntity.class.getSimpleName()),
                                el, AT_KEY_TYPE_CLASS, "value");
                    }
                }
            }

            return allPassed;
        }
    }

    private void printMessageWithAnnotationHint(final Kind kind, final String msg, final Element element, final Class<? extends Annotation> annotationType, final String annotationElementName) {
        final AnnotationMirror annotMirror = elementFinder.getElementAnnotationMirror(element, annotationType);
        if (annotMirror == null) {
            // simplest form of message that is present directly on the element
            messager.printMessage(kind, msg, element);
        }
        else {
            final Optional<AnnotationValue> annotElementValue = elementFinder.getAnnotationValue(annotMirror, annotationElementName);
            if (annotElementValue.isPresent()) {
                // fullest form of error message present on the element's annotation element value
                messager.printMessage(kind, msg, element, annotMirror, annotElementValue.get());
            }
            else {
                // useful message for debugging
                messager.printMessage(Kind.OTHER, "ANOMALY: AnnotationValue [%s.%s()] was absent. Element: %s. Annotation: %s."
                        .formatted(annotMirror.getAnnotationType().asElement().getSimpleName(), annotationElementName, element.getSimpleName(), annotMirror.toString()));
                // error message present on the element's annotation
                messager.printMessage(kind, msg, element, annotMirror);
            }
        }
    }

}