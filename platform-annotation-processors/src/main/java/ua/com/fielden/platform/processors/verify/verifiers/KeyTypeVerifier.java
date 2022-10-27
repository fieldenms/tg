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

    private final List<AbstractComposableVerifierPart> verifiers = List.of(new KeyTypePresence());

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
}