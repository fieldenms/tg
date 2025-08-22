package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.processors.appdomain.RegisteredEntitiesCollector;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.utils.TypeSet;
import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.*;

/**
 * Composable verifier for entity properties, responsible for the most essential verification, which includes:
 * <ol>
 *  <li>Correctness of property accessor and setter definitions</li>
 *  <li>Declaration of collectional properties as {@code final} fields</li>
 *  <li>Verification of property types</li>
 * </ol>
 * Properties with {@linkplain ErrorType unresolved types} are not subject to verification because erroneous definitions
 * are inherently incorrect.
 *
 * @author TG Team
 */
public class EssentialPropertyVerifier extends AbstractComposableEntityVerifier {

    public EssentialPropertyVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected List<AbstractEntityVerifier> createComponents(final ProcessingEnvironment procEnv) {
        return List.of(
                new PropertyAccessorVerifier(procEnv),
                new PropertySetterVerifier(procEnv),
                new CollectionalPropertyVerifier(procEnv),
                new PropertyTypeVerifier(procEnv),
                new RichTextPropertyVerifier(procEnv),
                new UnionEntityTypedKeyVerifier(procEnv));
    }

    /**
     * All properties must have a corresponding accessor method with a name starting with "get" or "is". The latter prefix
     * should be used strictly for {@code boolean} properties.
     * <p>
     * An accessor's return type must match its property type with the exception of collectional properties, where the
     * return type must be <b>assignable to</b> the property type.
     */
    static class PropertyAccessorVerifier extends AbstractEntityVerifier {

        public static final String errMissingAccessor(final String propName) {
            return "Missing accessor for property [%s].".formatted(propName);
        }

        public static final String errIncorrectReturnType(final String accessorName, final String propertyType) {
            return "Accessor [%s] must have return type %s".formatted(accessorName, propertyType);
        }

        public static final String errCollectionalIncorrectReturnType(final String accessorName, final String propertyType) {
            return "Accessor [%s] must have return type assignable from %s and parameterised with the collection element type"
                    .formatted(accessorName, propertyType);
        }

        protected PropertyAccessorVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingDeclaredProperties(new PropertyVerifier(entityFinder));
        }

        private class PropertyVerifier extends AbstractPropertyElementVerifier {
            public PropertyVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                if (hasErrorType(property)) {
                    return empty();
                }

                // accessor must be declared
                final Optional<ExecutableElement> maybeAccessor = entityFinder.findDeclaredPropertyAccessor(entity, getSimpleName(property.element()));
                if (maybeAccessor.isEmpty()) {
                    return of(new ViolatingElement(property.element(), Kind.ERROR, errMissingAccessor(getSimpleName(property.element()))));
                }

                final ExecutableElement accessor = maybeAccessor.get();

                // collectional properties
                if (entityFinder.isCollectionalProperty(property)) {
                    // acessor's return type must be assignable from the property type,
                    // i.e., property type should be a subtype of the accessor's return type
                    // AND the return type must be parameterised
                    if (isRawType(accessor.getReturnType()) ||
                            !elementFinder.types.isSubtype(property.getType(), accessor.getReturnType())) {
                        return of(new ViolatingElement(
                                accessor, Kind.ERROR, errCollectionalIncorrectReturnType(getSimpleName(accessor), property.getType().toString())));
                    }
                }
                else { /* other properties */
                    // acessor's return type must match the property type
                    if (!elementFinder.types.isSameType(accessor.getReturnType(), property.getType())) {
                        return of(new ViolatingElement(
                                accessor, Kind.ERROR, errIncorrectReturnType(getSimpleName(accessor), property.getType().toString())));
                    }
                }

                return empty();
            }
        }

    }

    /**
     * All properties must have a coresponding setter that is either public or protected, and annotated with {@link Observable}.
     * A setter must accept a single argument of its property type.
     */
    static class PropertySetterVerifier extends AbstractEntityVerifier {
        private static final Class<Observable> AT_OBSERVABLE_CLASS = Observable.class;

        protected PropertySetterVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public static final String errMissingSetter(final String propName) {
            return "Missing setter for property [%s].".formatted(propName);
        }

        public static final String errMissingObservable(final String setterName) {
            return "Missing @%s for property setter [%s].".formatted(AT_OBSERVABLE_CLASS.getSimpleName(), setterName);
        }

        public static final String errNotPublicNorProtected(final String setterName) {
            return "Setter [%s] must be declared public or protected".formatted(setterName);
        }

        public static final String errIncorrectParameters(final String setterName, final String propertyType) {
            return "Setter [%s] must declare a single parameter of type %s".formatted(setterName, propertyType);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingDeclaredProperties(new PropertyVerifier(entityFinder));
        }

        private class PropertyVerifier extends AbstractPropertyElementVerifier {
            public PropertyVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                if (hasErrorType(property)) {
                    return empty();
                }

                // setter should be declared
                final Optional<ExecutableElement> maybeSetter = entityFinder.findDeclaredPropertySetter(entity, getSimpleName(property.element()));
                if (maybeSetter.isEmpty()) {
                    return of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMissingSetter(getSimpleName(property.element()))));
                }

                final ExecutableElement setter = maybeSetter.get();
                // should be annotated with @Observable
                if (setter.getAnnotation(AT_OBSERVABLE_CLASS) == null) {
                    return of(new ViolatingElement(setter, Kind.ERROR, errMissingObservable(getSimpleName(setter))));
                }

                // should be public or protected
                if (!ElementFinder.isPublic(setter) && !ElementFinder.isProtected(setter)) {
                    return of(new ViolatingElement(setter, Kind.ERROR, errNotPublicNorProtected(getSimpleName(setter))));
                }

                // should accept single argument of the property type
                final List<? extends VariableElement> params =  setter.getParameters();
                if (params.size() != 1 || !elementFinder.types.isSameType(params.get(0).asType(), property.getType())) {
                    return of(new ViolatingElement(
                            setter, Kind.ERROR, errIncorrectParameters(getSimpleName(setter), property.getType().toString())));
                }

                return empty();
            }
        }
    }

    /**
     * Collectional properties must be declared final.
     */
    static class CollectionalPropertyVerifier extends AbstractEntityVerifier {
        protected CollectionalPropertyVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public static final String errMustBeFinal(final String propName) {
            return "Collectional property [%s] must be declared final.".formatted(propName);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingDeclaredProperties(new PropertyVerifier(entityFinder));
        }

        private class PropertyVerifier extends AbstractPropertyElementVerifier {
            public PropertyVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                if (hasErrorType(property)) {
                    return empty();
                }
                if (!entityFinder.isCollectionalProperty(property)) {
                    return empty();
                }
                if (!EntityFinder.isFinal(property.element())) {
                    return of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMustBeFinal(getSimpleName(property.element()))));
                }
                return empty();
            }
        }
    }

    /**
     * Acceptable property types:
     * <ol>
     *   <li>Ordinary (aka primitive) types: {@link Long}, {@link Integer}, {@link BigDecimal}, {@link Date}, {@link String}, {@code boolean}.
     *   <li>Custom platform types: {@link Money}, {@link Colour}, {@link Hyperlink}, {@link RichText}.
     *   <li>Entity types:
     *   <ol>
     *      <li>Any registered domain entity (at the time of writing, this means an entity, registered in an
     *      application-specific class {@code ApplicationDomain}).
     *      <li>{@linkplain PlatformDomainTypes#types Platform entity types}.
     *      <li>Special entity types: {@link PropertyDescriptor}.
     *   </ol>

     *   <li>Collectional types: any type assignable to {@link java.util.Collection}, but parameterised with any of the
     *   permitted types in items 1-3.
     *   <li>Binary: {@code byte[]}.
     *   <li>Special case collectional: {@link Map} (key and value type verification will need to be covered by another verifier).
     * </ol>
     */
    static class PropertyTypeVerifier extends AbstractEntityVerifier {
        static final TypeSet ORDINARY_TYPES = TypeSet.ofClasses(String.class, Long.class, Integer.class, BigDecimal.class, Date.class, boolean.class);
        // includes boxed boolean
        static final TypeSet ORDINARY_TYPE_ARGS = TypeSet.ofClasses(String.class, Long.class, Integer.class, BigDecimal.class, Date.class, Boolean.class);
        static final TypeSet PLATFORM_TYPES = TypeSet.ofClasses(Money.class, Colour.class, Hyperlink.class, RichText.class);
        static final TypeSet BINARY_TYPES = TypeSet.ofClasses(byte[].class);
        static final TypeSet SPECIAL_ENTITY_TYPES = TypeSet.ofClasses(PropertyDescriptor.class);
        static final TypeSet PLATFORM_ENTITY_TYPES = TypeSet.ofClasses(PlatformDomainTypes.types);
        static final List<Class<?>> SPECIAL_COLLECTION_TYPES = List.of(Map.class);

        protected PropertyTypeVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public static String errEntityTypeMustBeRegistered(final String property, final String type) {
            return "Property [%s] is of unregistered entity type [%s]. Entity types must be registered before use as property types.".formatted(property, type);
        }

        public static String errEntityTypeArgMustBeRegistered(final String property, final String type) {
            return "The type of property [%s] is parameterised with unregistered entity type [%s]. Entity types must be registered before use as property type arguments.".formatted(property, type);
        }

        public static String errInvalidCollectionTypeArg(final String property) {
            return "Collectional property [%s] is parameterised with an unsupported type.".formatted(property);
        }

        public static String errUnsupportedType(final String property) {
            return ("Unsupported type for property [%s].\nSupported types include: %s, collectional and domain entity types.")
                    .formatted(property, MSG_SUPPORTED_TYPES);
        }

        private static final String MSG_SUPPORTED_TYPES =
                Stream.concat(Stream.of(ORDINARY_TYPES, PLATFORM_TYPES, BINARY_TYPES, SPECIAL_ENTITY_TYPES).flatMap(TypeSet::streamTypeNames),
                              SPECIAL_COLLECTION_TYPES.stream().map(Class::getSimpleName))
                        .map(name -> {
                            final var simpleName = substringAfterLast(name, '.');
                            return simpleName.isEmpty() ? name : simpleName;
                        })
                        .collect(Collectors.joining(", "));

        private boolean isSpecialCollectionType(final TypeMirror t) {
            return SPECIAL_COLLECTION_TYPES.stream().anyMatch(cls -> entityFinder.isSubtype(t, cls));
        }

        private boolean isAnyOf(final TypeMirror t, final TypeSet set) {
            return set.contains(t);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingDeclaredProperties(new PropertyVerifier(entityFinder, roundEnv.getRoundEnvironment()));
        }

        private class PropertyVerifier extends AbstractPropertyElementVerifier {
            private final RoundEnvironment roundEnv;

            public PropertyVerifier(final EntityFinder entityFinder, final RoundEnvironment roundEnv) {
                super(entityFinder);
                this.roundEnv = roundEnv;
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                if (hasErrorType(property)) {
                    return empty();
                }

                final TypeMirror propType = property.getType();

                // 1. ordinary type
                if (isAnyOf(propType, ORDINARY_TYPES)) return empty();
                // 2. platform type
                if (isAnyOf(propType, PLATFORM_TYPES)) return empty();
                // 5. binary type
                if (isAnyOf(propType, BINARY_TYPES)) return empty();
                // 6. special case of collection-like types
                if (isSpecialCollectionType(propType)) return empty();
                // 3.2
                if (isAnyOf(propType, SPECIAL_ENTITY_TYPES)) return empty();
                // 3.3
                if (isAnyOf(propType, PLATFORM_ENTITY_TYPES)) return empty();

                if (entityFinder.isEntityType(propType)) {
                    final EntityElement propTypeEntityElt = entityFinder.newEntityElement(asTypeElementOfTypeMirror(propType));
                    // 3.1 all entity types, except some special ones, used as property types must be registered
                    if (propTypeEntityElt.isAbstract() || isEntityTypeRegistered(propTypeEntityElt)) {
                        return empty();
                    } else {
                        return of(new ViolatingElement(
                                property.element(), Kind.ERROR,
                                errEntityTypeMustBeRegistered(getSimpleName(property.element()), getSimpleName(propTypeEntityElt))));
                    }
                }

                // 4. collectional type
                if (entityFinder.isCollectionalProperty(property)) {
                    // check type arguments
                    final List<? extends TypeMirror> typeArguments = asDeclaredType(propType).getTypeArguments();
                    // collection types accept a single type argument
                    if (!typeArguments.isEmpty()) {
                        final TypeMirror typeArg = typeArguments.get(0);

                        if (isAnyOf(typeArg, ORDINARY_TYPE_ARGS)
                                || isAnyOf(typeArg, PLATFORM_TYPES)
                                || isAnyOf(typeArg, PLATFORM_ENTITY_TYPES)
                                || isAnyOf(typeArg, SPECIAL_ENTITY_TYPES)) {
                            return empty();
                        }

                        if (entityFinder.isEntityType(typeArg)) {
                            final EntityElement entityTypeElt = entityFinder.newEntityElement(asTypeElementOfTypeMirror(typeArg));
                            if (entityTypeElt.isAbstract() || isEntityTypeRegistered(entityTypeElt)) {
                                return empty();
                            } else {
                                return of(new ViolatingElement(
                                        property.element(), Kind.ERROR,
                                        errEntityTypeArgMustBeRegistered(getSimpleName(property.element()), getSimpleName(typeArg))));
                            }
                        }
                        // all valid type arguments were exhausted
                        return of(new ViolatingElement(
                                property.element(), Kind.ERROR, errInvalidCollectionTypeArg(getSimpleName(property.element()))));
                    }
                    return empty(); // TODO process raw collection types
                }

                // all supported types were exhausted
                return of(new ViolatingElement(property.element(), Kind.ERROR, errUnsupportedType(getSimpleName(property.element()))));
            }

            /**
             * Tests whether an entity type is registered in {@code ApplicationDomain}.
             */
            private boolean isEntityTypeRegistered(final EntityElement entityElement) {
                return registeredEntities.apply(roundEnv).contains(entityElement);
            }
            // with memoized
            private Function<RoundEnvironment, Set<EntityElement>> registeredEntities = roundEnv -> {
                final var result = new HashSet<EntityElement>();
                RegisteredEntitiesCollector.getInstance(processingEnv)
                        .withSuppressedMessages(it -> it.collectRegisteredEntities(roundEnv, result::add, result::add));
                this.registeredEntities = $ -> result;
                return result;
            };
        }
    }

    /**
     * Verifies properties with type {@link RichText}:
     * <ol>
     *   <li> Cannot be a part of entity key, i.e., a composite key member.
     * </ol>
     */
    static class RichTextPropertyVerifier extends AbstractEntityVerifier {

        protected RichTextPropertyVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingDeclaredProperties(new PropertyVerifier(entityFinder));
        }

        private class PropertyVerifier extends AbstractPropertyElementVerifier {
            PropertyVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                if (!isSameType(property.getType(), RichText.class)) {
                    return Optional.empty();
                }

                if (entityFinder.isKeyMember(property)) {
                    return of(new ViolatingElement(property.element(), Kind.ERROR,
                                                   errKeyMemberRichText(getSimpleName(entity.element()), getSimpleName(property.element()))));
                }

                return Optional.empty();
            }
        }

        public static String errKeyMemberRichText(final String entity, final String property) {
            return "RichText property [%s] cannot be used as a key member.".formatted(entity + "." + property);
        }
    }

    /**
     * Union entity types cannot be used for property {@code key}.
     */
    static final class UnionEntityTypedKeyVerifier extends AbstractEntityVerifier {
        UnionEntityTypedKeyVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public static final String ERR_UNION_ENTITY_TYPED_SIMPLE_KEY = "Union entity types are unsupported for property [key].";

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingDeclaredProperties(new PropertyVerifier(entityFinder));
        }

        private static class PropertyVerifier extends AbstractPropertyElementVerifier {
            public PropertyVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                if (hasErrorType(property)) {
                    return empty();
                }

                if (property.getSimpleName().contentEquals(KEY) && entityFinder.isUnionEntityType(property.getType())) {
                    return of(new ViolatingElement(property.element(), Kind.ERROR, ERR_UNION_ENTITY_TYPED_SIMPLE_KEY));
                }

                return empty();
            }
        }
    }

}
