package ua.com.fielden.platform.meta;

import jakarta.annotation.Nullable;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.meta.DomainMetadataGenerator.hasAnyNature;
import static ua.com.fielden.platform.meta.EntityNature.SYNTHETIC;
import static ua.com.fielden.platform.meta.EntityNature.UNION;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.classFrom;

// TODO merge with HibernateTypeDeterminer
// TODO once dependency injection is properly configured, refactor this class into a service that provides the following API:
// Optional<Object> hibernateType(PropertyMetadata)
class HibernateTypeGenerator {

    // NOTE This came from old code and it's unclear whether this is the right way of obtaining Hibernate types.
    private static final TypeConfiguration typeConfiguration = new TypeConfiguration();
    private static final TypeResolver typeResolver = new TypeResolver(typeConfiguration, new TypeFactory(typeConfiguration));

    private final HibernateTypeMappings hibernateTypeMappings;

    HibernateTypeGenerator(final HibernateTypeMappings hibernateTypeMappings) {
        this.hibernateTypeMappings = hibernateTypeMappings;
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
                        } else if (!Void.class.equals(hibernateUserTypeImplementor)) {
                            // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
                            return hibernateTypeMappings.getHibernateType(hibernateUserTypeImplementor)
                                    .orElseThrow(() -> new DomainMetadataGenerationException(
                                            format("Couldn't obtain instance of Hibernate type [%s]", hibernateUserTypeImplementor)));
                        } else {
                            throw new DomainMetadataGenerationException(
                                    format("Annotation [%s] doesn't provide enough information to obtain a Hibernate type.",
                                           atPersistentType));
                        }
                    }).orElseGet(() -> {
                        // this helps us get the raw class of parameterized types such as PropertyDescriptor
                        final Class<?> klass = classFrom(typeMetadata.javaType());
                        if (klass != null) {
                            return hibernateTypeMappings.getHibernateType(klass)
                                    // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                                    .orElseGet(() -> typeResolver.basic(klass.getName()));
                        }
                        return null;
                    });
                }
            };
        }

        private boolean isHibTypeApplicable(final PropertyTypeMetadata typeMetadata) {
            return (   typeMetadata.isPrimitive()
                    || typeMetadata.isCompositeKey() // mkPropKey handles composite keys
                    || typeMetadata.isComponent()
                    || typeMetadata.isEntity());
        }
    }

}
