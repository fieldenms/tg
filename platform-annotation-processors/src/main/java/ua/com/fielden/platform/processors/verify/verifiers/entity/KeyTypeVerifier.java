package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.entity.*;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.utils.TypeSet;
import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;
import ua.com.fielden.platform.web.action.AbstractFunEntityForDataExport;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.findAnnotationMirror;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isSameType;

/**
 * Performs verification of a domain model with respect to the {@link KeyType} annotation.
 * <p>
 * The following conditions will cause verification failure:
 * <ol>
 *  <li>Entity definition is missing {@link KeyType}, i.e., neither the entity type nor its super types have this annotation
 *  declared. Abstract entities should be able to have no {@link KeyType} annotation present.</li>
 *  <li>The type of key as defined by {@link KeyType} does not match the one specified as the type argument to {@link AbstractEntity}.</li>
 *  <li>Child entity declares {@link KeyType} that does not match the type at the super type level.</li>
 *  <li>Entity declares property {@code key} having a type that does not match the one defined by {@link KeyType}.
 *      Additionally, if {@link NoKey} is specified, then it's forbidden to declare property {@code key}.</li>
 * </ol>
 * Most contexts where an {@linkplain ErrorType unresolved type} is encountered are not subject to verification because
 * erroneous definitions are inherently incorrect.
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

        static final String ERR_ENTITY_DEFINITION_IS_MISSING_KEY_TYPE = "Entity definition is missing @%s.".formatted(AT_KEY_TYPE_CLASS.getSimpleName());
        static final String ERR_KEY_TYPE_DEFINITION_REFERENCES_UNION_ENTITY = "Union entity types are not supported for @%s.".formatted(AT_KEY_TYPE_CLASS.getSimpleName());

        protected KeyTypePresence(ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        public List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingElements(new EntityVerifier(entityFinder));
        }

        private static class EntityVerifier extends AbstractEntityElementVerifier {

            public EntityVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verify(final EntityElement entity) {
                // Concrete entity types must have @KeyType
                if (!ElementFinder.isAbstract(entity) && entityFinder.findAnnotation(entity, AT_KEY_TYPE_CLASS).isEmpty()) {
                    return Optional.of(new ViolatingElement(entity.element(), Kind.ERROR, ERR_ENTITY_DEFINITION_IS_MISSING_KEY_TYPE));
                }

                // If @KeyType is present, then we need to verify that it is not of a Union Entity type.
                final Optional<TypeMirror> maybeKeyType = entityFinder.determineKeyType(entity);
                if (maybeKeyType.isEmpty()) {
                    return Optional.empty();
                }
                final TypeMirror keyType = maybeKeyType.get();
                if (keyType.getKind() == TypeKind.ERROR) {
                    return Optional.empty();
                }

                // If the declared key type is of a Union Entity type, then report an error.
                if (entityFinder.isUnionEntityType(keyType)) {
                    return of(new ViolatingElement(entity.element(), Kind.ERROR, ERR_KEY_TYPE_DEFINITION_REFERENCES_UNION_ENTITY));
                }

                return Optional.empty();
            }
        }

    }

    // TODO Consider indirect parameterisation of AbstractEntity types
    // e.g. Sub extends Super<KeyType>, where Super<K> extends AbstractEntity<K>
    /**
     * The type of key as defined by {@link KeyType} must match the one specified as the type argument to the direct supertype,
     * if it is a member of the {@link AbstractEntity} type family (i.e., is parameterized with a key type).
     */
    static class KeyTypeValueMatchesAbstractEntityTypeArgument extends AbstractEntityVerifier {
        static final String SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE = "Supertype must be parameterized with entity key type.";
        static final String KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY = "Key type must match the supertype's type argument.";

        static final TypeSet ABSTRACTS = TypeSet.ofClasses(
                AbstractEntity.class, AbstractPersistentEntity.class, ActivatableAbstractEntity.class,
                AbstractFunctionalEntityWithCentreContext.class, AbstractEntityWithInputStream.class, AbstractTreeEntry.class,
                AbstractFunEntityForDataExport.class);

        protected KeyTypeValueMatchesAbstractEntityTypeArgument(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        private boolean isOneOfAbstracts(final TypeElement element) {
            return ABSTRACTS.contains(element.asType());
        }

        @Override
        public List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingElements(new EntityVerifier(entityFinder));
        }

        private class EntityVerifier extends AbstractEntityElementVerifier {

            public EntityVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verify(final EntityElement entity) {
                final Optional<? extends AnnotationMirror> maybeKeyTypeAnnotMirror = findAnnotationMirror(entity, AT_KEY_TYPE_CLASS);
                if (maybeKeyTypeAnnotMirror.isEmpty()) {
                    return Optional.empty();
                }

                final AnnotationMirror keyTypeAnnotMirror = maybeKeyTypeAnnotMirror.get();

                final Optional<EntityElement> maybeParent = entityFinder.getParent(entity);
                if (maybeParent.isEmpty()) {
                    return Optional.empty();
                }

                final EntityElement parent = maybeParent.get();
                if (!isOneOfAbstracts(parent)) {
                    return Optional.empty();
                }

                final AnnotationValue keyTypeAnnotValue = entityFinder.getKeyTypeAnnotationValue(keyTypeAnnotMirror);
                final TypeMirror actualKeyTypeMirror = (TypeMirror) keyTypeAnnotValue.getValue();
                if (actualKeyTypeMirror.getKind() == TypeKind.ERROR) {
                    return Optional.empty();
                }

                final DeclaredType entitySuperclassType = (DeclaredType) entity.getSuperclass();
                if (entitySuperclassType.getKind() == TypeKind.ERROR) {
                    return Optional.empty();
                }

                final List<? extends TypeMirror> parentTypeArgs = entitySuperclassType.getTypeArguments();
                if (parentTypeArgs.isEmpty()) {
                    return Optional.of(new ViolatingElement(entity.element(), Kind.ERROR, SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE));
                }
                // abstract entities accept a single type argument
                else {
                    final TypeMirror typeArg = parentTypeArgs.getFirst();
                    if (typeArg.getKind() == TypeKind.ERROR) {
                        return Optional.empty();
                    }

                    if (!typeUtils.isSameType(typeArg, actualKeyTypeMirror)) {
                        return Optional.of(new ViolatingElement(entity.element(), Kind.ERROR, KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY,
                                keyTypeAnnotMirror, keyTypeAnnotValue));
                    }
                }

                return Optional.empty();
            }
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

        @Override
        public List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingElements(new EntityVerifier(entityFinder));
        }

        private class EntityVerifier extends AbstractEntityElementVerifier {

            public EntityVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verify(final EntityElement entity) {
                final Optional<? extends AnnotationMirror> maybeEntityKeyTypeAnnotMirror = findAnnotationMirror(entity, AT_KEY_TYPE_CLASS);
                if (maybeEntityKeyTypeAnnotMirror.isEmpty()) {
                    return Optional.empty();
                }

                final AnnotationMirror entityKeyTypeAnnotMirror = maybeEntityKeyTypeAnnotMirror.get();
                final AnnotationValue entityKeyTypeAnnotValue = entityFinder.getKeyTypeAnnotationValue(entityKeyTypeAnnotMirror);
                final TypeMirror entityKeyTypeMirror = (TypeMirror) entityKeyTypeAnnotValue.getValue();
                if (entityKeyTypeMirror.getKind() == TypeKind.ERROR) {
                    return Optional.empty();
                }

                final Optional<EntityElement> maybeParent = entityFinder.getParent(entity);
                // skip non-child entities and those with an abstract parent
                // TODO handle hierarchy [non-abstract -> abstract -> non-abstract -> ...] ?
                if (maybeParent.map(elt -> ElementFinder.isAbstract(elt)).orElse(true)) {
                    return Optional.empty();
                }

                final EntityElement parent = maybeParent.get();
                final Optional<TypeMirror> parentKeyTypeMirror = entityFinder.determineKeyType(parent);
                // parent might be missing @KeyType, which should have been detected by KeyTypePresence verifier, so we ignore this case
                if (parentKeyTypeMirror.map(t -> t.getKind() == TypeKind.ERROR).orElse(true)) {
                    return Optional.empty();
                }

                if (!typeUtils.isSameType(parentKeyTypeMirror.get(), entityKeyTypeMirror)) {
                    return Optional.of(new ViolatingElement(entity.element(), Kind.ERROR,
                            keyTypeMustMatchTheSupertypesKeyType(parent.getSimpleName().toString()),
                            entityKeyTypeAnnotMirror, entityKeyTypeAnnotValue));
                }

                return Optional.empty();
            }
        }
    }

    /**
     * If an entity declares property {@code key}, then its type must match the key type defined by {@link KeyType}.
     * Additionally, if {@link NoKey} is specified, then it's forbidden to declare property {@code key}.
     */
    static class DeclaredKeyPropertyTypeMatchesAtKeyTypeValue extends AbstractEntityVerifier {
        static final String ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY = "Entity with NoKey as key type can not declare property [key].";
        static final String KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION = "The [key] property type must be consistent with @KeyType definition.";

        protected DeclaredKeyPropertyTypeMatchesAtKeyTypeValue(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        public List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingElements(new EntityVerifier(entityFinder));
        }

        private class EntityVerifier extends AbstractEntityElementVerifier {

            public EntityVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verify(final EntityElement entity) {
                final Optional<PropertyElement> maybeKeyProp = entityFinder.findDeclaredProperty(entity, AbstractEntity.KEY);
                if (maybeKeyProp.isEmpty()) {
                    return Optional.empty();
                }

                final PropertyElement keyProp = maybeKeyProp.get();
                final Optional<TypeMirror> maybeKeyType = entityFinder.determineKeyType(entity);
                // missing @KeyType could mean an abstract entity or an invalid definition, either way this verifier has other responsibilities
                if (maybeKeyType.isEmpty()) {
                    return Optional.empty();
                }
                final TypeMirror keyType = maybeKeyType.get();
                if (keyType.getKind() == TypeKind.ERROR) {
                    return Optional.empty();
                }

                // keyProp might have an unresolved type but we still let this verifier run because declaration of property
                // key along with @KeyType(NoKey.class) is incorrect regardless of its type
                if (isSameType(keyType, NoKey.class)) {
                    return Optional.of(new ViolatingElement(keyProp.element(), Kind.ERROR,
                            ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY));
                } else {
                    final TypeMirror keyPropType = keyProp.getType();
                    if (keyPropType.getKind() == TypeKind.ERROR) {
                        return Optional.empty();
                    } else if (!typeUtils.isSameType(keyPropType, keyType)) {
                        return Optional.of(new ViolatingElement(keyProp.element(), Kind.ERROR,
                                KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION));
                    }
                }

                return Optional.empty();
            }
        }
    }

}
