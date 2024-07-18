package ua.com.fielden.platform.meta;

import com.google.inject.Injector;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.meta.DomainMetadataGenerator.hasAnyNature;
import static ua.com.fielden.platform.meta.EntityNature.SYNTHETIC;
import static ua.com.fielden.platform.meta.EntityNature.UNION;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_BOOLEAN;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.classFrom;

// TODO merge with HibernateTypeDeterminer
// TODO once dependency injection is properly configured, refactor this class into a service that provides the following API:
// Optional<Object> hibernateType(PropertyMetadata)
class HibernateTypeGenerator {

    // NOTE This came from old code and it's unclear whether this is the right way of obtaining Hibernate types.
    private static final TypeConfiguration typeConfiguration = new TypeConfiguration();
    private static final TypeResolver typeResolver = new TypeResolver(typeConfiguration, new TypeFactory(typeConfiguration));

    /** Class-to-instance map for Hibernate types. */
    private final Map<Class<?>, Object> hibTypesDefaults;
    private final Injector hibTypesInjector;

    HibernateTypeGenerator(final Map<? extends Class, ? extends Class> hibTypesDefaults, final Injector hibTypesInjector) {
        this.hibTypesInjector = hibTypesInjector;

        if (hibTypesDefaults != null) {
            final var map = new HashMap<Class<?>, Object>();
            hibTypesDefaults.forEach((javaType, hibType) -> {
                try {
                    map.put(javaType, hibType.getDeclaredField("INSTANCE").get(null));
                } catch (final Exception e) {
                    throw new DomainMetadataGenerationException("Couldn't instantiate Hibernate type [" + hibType + "].",
                                                                e);
                }
            });
            // TODO old code, definitely a kludge, this class should not be responsible for establishing any mappings
            map.put(Boolean.class, H_BOOLEAN);
            map.put(boolean.class, H_BOOLEAN);
            this.hibTypesDefaults = unmodifiableMap(map);
        } else {
            this.hibTypesDefaults = Map.of();
        }
    }

    /**
     * Resolves a Hibernate type for a property.
     */
    public Generate generate(final PropertyTypeMetadata typeMetadata) {
        return new Generate(typeMetadata);
    }

    /**
     * An abstraction for a method with optional parameters. Helps avoid having multiple signatures of the same method.
     *
     * @see #generate(PropertyTypeMetadata)
     */
    public class Generate {
        private final PropertyTypeMetadata typeMetadata;
        private Optional<PersistentType> optAtPersistentType = Optional.empty();

        private Generate(final PropertyTypeMetadata typeMetadata) {
            this.typeMetadata = typeMetadata;
        }

        /**
         * Returns the Hibernate type or throws if it could not be resolved.
         */
        public Object get() {
            return getOpt()
                    .orElseThrow(() -> new DomainMetadataGenerationException(format("Couldn't resolve Hibernate type of [%s]", typeMetadata)));
        }

        public Optional<Object> getOpt() {
            return Optional.ofNullable(get_());
        }

        public Generate use(final PersistentType atPersistentType) {
            this.optAtPersistentType = Optional.of(atPersistentType);
            return this;
        }

        public Generate use(final Field field) {
            this.optAtPersistentType = AnnotationReflector.getAnnotationOptionally(field, PersistentType.class);
            return this;
        }

        @Nullable Object get_() {
            if (!isHibTypeApplicable(typeMetadata)) {
                return null;
            }

            return switch (typeMetadata) {
                case PropertyTypeMetadata.Entity et
                        when hasAnyNature(et.javaType(), List.of(UNION, EntityNature.PERSISTENT, SYNTHETIC))
                        -> H_ENTITY;
                default -> {
                    yield optAtPersistentType.map(atPersistentType -> {
                        final String hibernateTypeName = atPersistentType.value();
                        final Class<?> hibernateUserTypeImplementor = atPersistentType.userType();
                        if (isNotEmpty(hibernateTypeName)) {
                            return typeResolver.basic(hibernateTypeName);
                        } else if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) {
                            // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
                            try {
                                // need to have the same instance for unit tests
                                return hibTypesInjector.getInstance(hibernateUserTypeImplementor).getClass()
                                        .getDeclaredField("INSTANCE").get(null);
                            } catch (final Exception e) {
                                throw new DomainMetadataGenerationException(
                                        format("Couldn't obtain instance of Hibernate type [%s]", hibernateUserTypeImplementor),
                                        e);
                            }
                        } else {
                            throw new DomainMetadataGenerationException(
                                    format("Annotation [%s] doesn't provide enough information to obtain a Hibernate type.",
                                           atPersistentType));
                        }
                    }).orElseGet(() -> {
                        // this helps us get the raw class of parameterized types such as PropertyDescriptor
                        final Class<?> klass = classFrom(typeMetadata.javaType());
                        if (klass != null) {
                            final Object defaultHibType = hibTypesDefaults.get(klass);
                            if (defaultHibType != null) { // default is provided for given property java type
                                return defaultHibType;
                            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                                return typeResolver.basic(klass.getName());
                            }
                        }
                        return null;
                    });
                }
            };
        }

        private boolean isHibTypeApplicable(final PropertyTypeMetadata typeMetadata) {
            return (   typeMetadata.isPrimitive()
                    || typeMetadata.isCompositeKey() // mkPropKey handles composite keys
                    || typeMetadata.isComposite()
                    || typeMetadata.isEntity());
        }
    }

}
