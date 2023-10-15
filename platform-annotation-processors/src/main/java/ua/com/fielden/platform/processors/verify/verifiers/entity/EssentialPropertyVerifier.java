package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static java.util.Optional.of;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asDeclaredType;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.getSimpleName;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isRawType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

/**
 * Composable verifier for entity properties, responsible for the most essential verification, which includes:
 * <ol>
 *  <li>Correctness of property accessor and setter definitions</li>
 *  <li>Declaration of collectional properties as {@code final} fields</li>
 *  <li>Verification of property types</li>
 * </ol>
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
                new PropertyTypeVerifier(procEnv));
    }

    /**
     * All properties must have a corresponding accessor method with a name starting with "get" or "is".
     * <p>
     * An accessor's return type must match its property type with the exception of collectional properties, where return type must be
     * <b>assignable to</b> the property type.
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

                return Optional.empty();
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
                // setter should be declared
                final Optional<ExecutableElement> maybeSetter = entityFinder.findDeclaredPropertySetter(entity, getSimpleName(property.element()));
                if (maybeSetter.isEmpty()) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMissingSetter(getSimpleName(property.element()))));
                }
                final ExecutableElement setter = maybeSetter.get();

                // should be annotated with @Observable
                if (setter.getAnnotation(AT_OBSERVABLE_CLASS) == null) {
                    return Optional.of(new ViolatingElement(setter, Kind.ERROR, errMissingObservable(getSimpleName(setter))));
                }

                // should be public or protected
                if (!ElementFinder.isPublic(setter) && !ElementFinder.isProtected(setter)) {
                    return Optional.of(new ViolatingElement(setter, Kind.ERROR, errNotPublicNorProtected(getSimpleName(setter))));
                }

                // should accept single argument of the property type
                final List<? extends VariableElement> params =  setter.getParameters();
                if (params.size() != 1 || !elementFinder.types.isSameType(params.get(0).asType(), property.getType())) {
                    return Optional.of(new ViolatingElement(
                            setter, Kind.ERROR, errIncorrectParameters(getSimpleName(setter), property.getType().toString())));
                }

                return Optional.empty();
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
                if (!entityFinder.isCollectionalProperty(property)) {
                    return Optional.empty();
                }
                if (!EntityFinder.isFinal(property.element())) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMustBeFinal(getSimpleName(property.element()))));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Acceptable property types:
     * <ol>
     *   <li>Ordinary (aka primitive) types: {@link Long}, {@link Integer}, {@link BigDecimal}, {@link Date}, {@link String}, {@link boolean}.</li>
     *   <li>Custom platform types: {@link Money}, {@link Colour}, {@link Hyperlink}.</li>
     *   <li>Entity types: any registered domain entity
     *   (at the time of writing, this means an entity, registered in an application specific class {@link ApplicationDomain}).</li>
     *   <li>Collectional types: any type assignable to {@link java.util.Collection}, but parameterised with any of the permitted types
     *   in items 1-3.</li>
     *   <li>Binary: {@code byte[]}.</li>
     *   <li>Special case collectional: {@link Map} (key and value type verification will need to be covered by another verifier).</li>
     * </ol>
     */
    static class PropertyTypeVerifier extends AbstractEntityVerifier {
        static final List<Class<?>> ORDINARY_TYPES = List.of(String.class, Long.class, Integer.class, BigDecimal.class, Date.class, boolean.class);
        // includes boxed boolean
        static final List<Class<?>> ORDINARY_TYPE_ARGS = List.of(String.class, Long.class, Integer.class, BigDecimal.class, Date.class, Boolean.class);
        static final List<Class<?>> PLATFORM_TYPES = List.of(Money.class, Colour.class, Hyperlink.class);
        static final List<Class<?>> BINARY_TYPES = List.of(byte[].class);
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
            return ("Unsupported type for property [%s]." + "\n" +
                    "Supported types include: String, Long, Integer, BigDecimal, Date, boolean, Money, Colour, Hyperlink, byte[], Set, List, Map, and Entity Types.")
                    .formatted(property);
        }

        private boolean isSpecialCollectionType(final TypeMirror t) {
            return SPECIAL_COLLECTION_TYPES.stream().anyMatch(cls -> entityFinder.isSubtype(t, cls));
        }

        private boolean isAnyOf(final TypeMirror t, final List<Class<?>> classes) {
            return classes.stream().anyMatch(cls -> entityFinder.isSameType(t, cls));
        }

        private boolean isEntityTypeRegistered(final TypeMirror entityType) {
            // TODO Implement when ApplicationDomain becomes analysable by annotation processors or
            // some other suitable entity registration mechanism is used.
            // Currently, entity types are registered in the static initialiser block, which is unreachable to annotation processors.
            // Refer issue https://github.com/fieldenms/tg/issues/1946
            return true;
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
                final TypeMirror propType = property.getType();

                // 1. ordinary type
                if (isAnyOf(propType, ORDINARY_TYPES)) return Optional.empty();
                // 2. platform type
                if (isAnyOf(propType, PLATFORM_TYPES)) return Optional.empty();
                // 5. binary type
                if (isAnyOf(propType, BINARY_TYPES)) return Optional.empty();
                // 6. special case of collection-like types
                if (isSpecialCollectionType(propType)) return Optional.empty();

                // 3. entity type
                if (entityFinder.isEntityType(propType)) {
                    if (!isEntityTypeRegistered(propType)) {
                        return Optional.of(new ViolatingElement(
                                property.element(), Kind.ERROR,
                                errEntityTypeMustBeRegistered(getSimpleName(property.element()), getSimpleName(propType))));
                    }
                    return Optional.empty();
                }

                // 4. collectional type
                if (entityFinder.isCollectionalProperty(property)) {
                    // check type arguments
                    final List<? extends TypeMirror> typeArguments = asDeclaredType(propType).getTypeArguments();
                    // collection types accept a single type argument
                    if (!typeArguments.isEmpty()) {
                        final TypeMirror typeArg = typeArguments.get(0);

                        if (isAnyOf(typeArg, ORDINARY_TYPE_ARGS) || isAnyOf(typeArg, PLATFORM_TYPES)) {
                            return Optional.empty();
                        }

                        if (entityFinder.isEntityType(typeArg)) {
                            return isEntityTypeRegistered(typeArg) ? Optional.empty() :
                                Optional.of(new ViolatingElement(
                                        property.element(), Kind.ERROR,
                                        errEntityTypeArgMustBeRegistered(getSimpleName(property.element()), getSimpleName(typeArg))));
                        }
                        // all valid type arguments were exhausted
                        return Optional.of(new ViolatingElement(
                                property.element(), Kind.ERROR, errInvalidCollectionTypeArg(getSimpleName(property.element()))));
                    }
                    return Optional.empty(); // TODO process raw collection types
                }

                // all supported types were exhausted
                return Optional.of(new ViolatingElement(property.element(), Kind.ERROR, errUnsupportedType(getSimpleName(property.element()))));
            }

        }
    }

}
