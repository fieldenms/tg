package ua.com.fielden.platform.processors.verify.verifiers;

import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
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
import ua.com.fielden.platform.processors.verify.verifiers.entity.AbstractComposableEntityVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.AbstractEntityVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EntityRoundEnvironment;
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
public class KeyTypeVerifier extends AbstractComposableEntityVerifier {

    static final Class<KeyType> AT_KEY_TYPE_CLASS = KeyType.class;

    public KeyTypeVerifier(final ProcessingEnvironment procEnv) {
        super(procEnv);
    }

    @Override
    protected List<AbstractEntityVerifier> createComponents(final ProcessingEnvironment procEnv) {
        return List.of(
                new KeyTypePresence(procEnv),
                new KeyTypeValueMatchesAbstractEntityTypeArgument(procEnv),
                new ChildKeyTypeMatchesParentKeyType(procEnv),
                new DeclaredKeyPropertyTypeMatchesAtKeyTypeValue(procEnv));
    }

    /**
     * Entity definition must include {@link KeyType} i.e., the entity type itself or its super types must have this annotation declared. 
     * Abstract entities should be able to omit this annotation.
     * 
     * @author TG Team
     */
    static class KeyTypePresence extends AbstractEntityVerifier {

        static final String ENTITY_DEFINITION_IS_MISSING_KEY_TYPE = "Entity definition is missing @%s.".formatted(
                AT_KEY_TYPE_CLASS.getSimpleName());

        protected KeyTypePresence(ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public boolean verify(final EntityRoundEnvironment roundEnv) {
            final List<EntityElement> entitiesMissingKeyType = roundEnv.listEntities().stream()
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

    // TODO Consider indirect parameterisation of AbstractEntity types
    // e.g. Sub extends Super<KeyType>, where Super<K> extends AbstractEntity<K>
    /**
     * The type of key as defined by {@link KeyType} must match the one specified as the type argument to the direct supertype, 
     * if it is a member of the {@link AbstractEntity} type family (i.e. is parameterized with a key type).
     */
    static class KeyTypeValueMatchesAbstractEntityTypeArgument extends AbstractEntityVerifier {
        static final String SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE = "Supertype must be parameterized with entity key type.";
        static final String KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY = "Key type must match the supertype's type argument.";

        static final List<Class<? extends AbstractEntity>> ABSTRACTS = List.of(
                AbstractEntity.class, AbstractPersistentEntity.class, ActivatableAbstractEntity.class,
                AbstractFunctionalEntityWithCentreContext.class, AbstractEntityWithInputStream.class, AbstractTreeEntry.class,
                AbstractFunEntityForDataExport.class);

        protected KeyTypeValueMatchesAbstractEntityTypeArgument(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        private boolean isOneOfAbstracts(final TypeElement element) {
            return ABSTRACTS.stream().anyMatch(clazz -> elementFinder.isSameType(element, clazz));
        }

        public boolean verify(final EntityRoundEnvironment roundEnv) {
            boolean allPassed = true;
            // we do not check that an element is an entity, since we assume that all elements annotated with @KeyType are entity types
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
    static class ChildKeyTypeMatchesParentKeyType extends AbstractEntityVerifier {
        static String keyTypeMustMatchTheSupertypesKeyType(final String supertypeSimpleName) {
            return "Key type must match the supertype's (%s) key type.".formatted(supertypeSimpleName);
        }

        protected ChildKeyTypeMatchesParentKeyType(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public boolean verify(final EntityRoundEnvironment roundEnv) {
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
    static class DeclaredKeyPropertyTypeMatchesAtKeyTypeValue extends AbstractEntityVerifier {
        static final String ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY = "Entity with NoKey as key type can not declare property \"key\".";
        static final String KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION = "\"key\" property type must be consistent with @KeyType definition.";

        protected DeclaredKeyPropertyTypeMatchesAtKeyTypeValue(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        public boolean verify(final EntityRoundEnvironment roundEnv) {
            boolean allPassed = true;

            for (final EntityElement entity : roundEnv.listEntities()) {
                final Optional<PropertyElement> maybeKeyProp = entityFinder.findDeclaredProperty(entity, AbstractEntity.KEY);
                if (maybeKeyProp.isEmpty()) continue;

                final PropertyElement keyProp = maybeKeyProp.get();
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

}
