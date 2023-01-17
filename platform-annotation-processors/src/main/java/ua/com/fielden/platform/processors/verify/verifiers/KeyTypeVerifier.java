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
import ua.com.fielden.platform.entity.AbstractEntityWithInputStream;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;
import ua.com.fielden.platform.web.action.AbstractFunEntityForDataExport;

/**
 * Performs verification of a domain model with respect to the {@link KeyType} annotation.
 * <p>
 * The following conditions will cause verification failure:
 * <ol>
 *  <li>Entity definition is missing {@link KeyType} i.e., neither the entity type nor its super types have this annotation declared. Abstract entities should be able have no {@link KeyType} annotation present.</li>
 *  <li>The type of key as defined by {@link KeyType} does not match the one specified as the type argument to {@link AbstractEntity}.</li>
 *  <li>Child entity declares {@link KeyType} that does not match the type at the super type level.</li>
 *  <li>Entity declares property {@code key} having a type that does not match the one defined by {@link KeyType}.
 *      Additionally, if {@link NoKey} is specified, then it's forbidden to declare property {@code key}.</li>
 * </ol> 
 * 
 * @author TG Team
 */
public class KeyTypeVerifier extends AbstractComposableVerifier {

    static final Class<KeyType> AT_KEY_TYPE_CLASS = KeyType.class;

    private final List<AbstractComposableVerifierPart> verifiers = List.of(
            new KeyTypePresence(),
            new KeyTypeValueMatchesAbstractEntityTypeArgument(),
            new ChildKeyTypeMatchesParentKeyType(),
            new DeclaredKeyPropertyTypeMatchesAtKeyTypeValue());

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
    class KeyTypePresence extends AbstractComposableVerifierPart {
        static final String ENTITY_DEFINITION_IS_MISSING_KEY_TYPE = "Entity definition is missing @%s.".formatted(
                AT_KEY_TYPE_CLASS.getSimpleName());

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
                    ENTITY_DEFINITION_IS_MISSING_KEY_TYPE, entity.element()));
            violatingElements.addAll(entitiesMissingKeyType);

            return entitiesMissingKeyType.isEmpty();
        }
    }

    /**
     * The type of key as defined by {@link KeyType} must match the one specified as the type argument to the direct supertype, 
     * if it is a member of the {@link AbstractEntity} type family (i.e. is parameterized with a key type).
     */
    class KeyTypeValueMatchesAbstractEntityTypeArgument extends AbstractComposableVerifierPart {
        static final String SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE = "Supertype must be parameterized with entity key type.";
        static final String KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY = "Key type must match the supertype's type argument.";

        static final List<Class<? extends AbstractEntity>> ABSTRACTS = List.of(
                AbstractEntity.class, AbstractPersistentEntity.class, ActivatableAbstractEntity.class,
                AbstractFunctionalEntityWithCentreContext.class, AbstractEntityWithInputStream.class, AbstractTreeEntry.class,
                AbstractFunEntityForDataExport.class);

        private boolean isOneOfAbstracts(final TypeElement element) {
            return ABSTRACTS.stream().anyMatch(clazz -> elementFinder.equals(element, clazz));
        }

        public boolean verify(final RoundEnvironment roundEnv) {
            boolean allPassed = true;
            final List<EntityElement> entitiesWithKeyType = roundEnv.getElementsAnnotatedWith(AT_KEY_TYPE_CLASS).stream()
                    .map(el -> entityFinder.newEntityElement((TypeElement) el))
                    .filter(el -> entityFinder.getParent(el).map(this::isOneOfAbstracts).orElse(false))
                    .toList();

            for (final EntityElement el : entitiesWithKeyType) {
                final TypeMirror keyType = entityFinder.getKeyType(el.getAnnotation(AT_KEY_TYPE_CLASS));
                final DeclaredType supertype = (DeclaredType) el.getSuperclass();

                final List<? extends TypeMirror> typeArgs = supertype.getTypeArguments();
                if (typeArgs.isEmpty()) {
                    violatingElements.add(el);
                    allPassed = false;
                    messager.printMessage(Kind.ERROR, SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE, el.element());
                }
                // abstract entities accept a single type argument
                else if (!typeUtils.isSameType(typeArgs.get(0), keyType)) {
                    violatingElements.add(el);
                    allPassed = false;
                    printMessageWithAnnotationHint(Kind.ERROR, KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY,
                            el.element(), AT_KEY_TYPE_CLASS, "value");
                }
            }

            return allPassed;
        }
    }

    /**
     * {@link KeyType} declared by a child entity must match the one declared at the super type level.
     * 
     * @author TG Team
     */
    class ChildKeyTypeMatchesParentKeyType extends AbstractComposableVerifierPart {
        static String keyTypeMustMatchTheSupertypesKeyType(final String supertypeSimpleName) {
            return "Key type must match the supertype's (%s) key type.".formatted(supertypeSimpleName);
        }

        public boolean verify(final RoundEnvironment roundEnv) {
            boolean allPassed = true;
            final List<EntityElement> entitiesWithDeclaredKeyType = roundEnv.getElementsAnnotatedWith(AT_KEY_TYPE_CLASS).stream()
                    .map(el -> entityFinder.newEntityElement((TypeElement) el))
                    .toList();

            for (final EntityElement entity : entitiesWithDeclaredKeyType) {
                final Optional<EntityElement> maybeParent = entityFinder.getParent(entity); 

                // skip non-child entities and those with an abstract parent
                // TODO handle hierarchy [non-abstract -> abstract -> non-abstract -> ...] ?
                if (maybeParent.map(elt -> elementFinder.isAbstract(elt)).orElse(true)) continue;

                final EntityElement parent = maybeParent.get();
                final Optional<KeyType> parentAtKeyType = entityFinder.findAnnotation(parent, AT_KEY_TYPE_CLASS);
                // parent might be missing @KeyType, which should have been detected by KeyTypePresence verifier-part, so we ignore this case
                if (parentAtKeyType.isEmpty()) continue;

                final TypeMirror parentKeyType = entityFinder.getKeyType(parentAtKeyType.get());
                final TypeMirror entityKeyType = entityFinder.getKeyType(entity.getAnnotation(AT_KEY_TYPE_CLASS));

                if (!typeUtils.isSameType(parentKeyType, entityKeyType)) {
                    violatingElements.add(entity);
                    allPassed = false;

                    // report error
                    printMessageWithAnnotationHint(Kind.ERROR, keyTypeMustMatchTheSupertypesKeyType(parent.getSimpleName().toString()),
                            entity.element(), AT_KEY_TYPE_CLASS, "value");
                }
            }

            return allPassed;
        }
    }
    
    /**
     * If an entity declares property {@code key}, then its type must match the key type defined by {@link KeyType}.
     * Additionally, if {@link NoKey} is specified, then it's forbidden to declare property {@code key}.
     */
    class DeclaredKeyPropertyTypeMatchesAtKeyTypeValue extends AbstractComposableVerifierPart {
        static final String ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY = "Entity with NoKey as key type can not declare property \"key\".";
        static final String KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION = "\"key\" property type must be consistent with @KeyType definition.";

        @Override
        public boolean verify(final RoundEnvironment roundEnv) {
            boolean allPassed = true;

            final List<EntityElement> entities = roundEnv.getRootElements().stream()
                    .filter(el -> elementFinder.isTopLevelClass(el))
                    .map(el -> (TypeElement) el)
                    .filter(el -> entityFinder.isEntityType(el))
                    .map(el -> entityFinder.newEntityElement(el))
                    .toList();
            
            for (final EntityElement entity : entities) {
                final PropertyElement keyProp = entityFinder.findDeclaredProperty(entity, AbstractEntity.KEY);
                if (keyProp == null) continue;

                final Optional<TypeMirror> maybeKeyType = entityFinder.determineKeyType(entity);
                // missing @KeyType could mean an abstract entity or an invalid definition, either way this verifier has other responsibilities
                if (maybeKeyType.isEmpty()) continue;
                
                final TypeMirror keyType = maybeKeyType.get();
                if (elementFinder.isSameType(keyType, NoKey.class)) {
                    allPassed = false;
                    this.violatingElements.add(entity);
                    messager.printMessage(Kind.ERROR, ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY, keyProp.element());
                }
                else {
                    final TypeMirror keyPropType = keyProp.getType();
                    if (!typeUtils.isSameType(keyPropType, keyType)) {
                        allPassed = false;
                        this.violatingElements.add(entity);
                        messager.printMessage(Kind.ERROR, KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION, keyProp.element());
                    }
                }
            }
            
            return allPassed;
        }
    }

    private void printMessageWithAnnotationHint(final Kind kind, final String msg, final Element element, final Class<? extends Annotation> annotationType, final String annotationElementName) {
        final Optional<? extends AnnotationMirror> maybeMirror = elementFinder.findAnnotationMirror(element, annotationType);
        if (maybeMirror.isEmpty()) {
            // simplest form of message that is present directly on the element
            messager.printMessage(kind, msg, element);
        }
        else {
            final AnnotationMirror mirror = maybeMirror.get();
            final Optional<AnnotationValue> annotElementValue = elementFinder.getAnnotationValue(mirror, annotationElementName);
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