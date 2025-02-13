package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPathToArray;

/**
 * The default implementation of {@link IDomainMetadata}.
 */
final class DomainMetadataImpl implements IDomainMetadata {

    public static final String ERR_ENTITY_TYPE_CANNOT_BE_SUBJECT_TO_METADATA_GENERATION = "Entity type [%s] cannot be subject to metadata generation.";

    private final DomainMetadataGenerator generator;
    private final PropertyMetadataUtils pmUtils;
    private final EntityMetadataUtils emUtils;

    DomainMetadataImpl(final DomainMetadataGenerator generator) {
        this.generator = generator;
        this.pmUtils = new PropertyMetadataUtilsImpl(this, generator);
        this.emUtils = new EntityMetadataUtilsImpl();
    }

    @Override
    public PropertyMetadataUtils propertyMetadataUtils() {
        return pmUtils;
    }

    @Override
    public EntityMetadataUtils entityMetadataUtils() {
        return emUtils;
    }

    @Override
    public Stream<TypeMetadata> allTypes() {
        return generator.allTypes();
    }

    @Override
    public <T extends TypeMetadata> Stream<T> allTypes(final Class<T> metadataType) {
        return generator.allTypes(metadataType);
    }

    @Override
    public Optional<? extends TypeMetadata> forType(final Class<?> javaType) {
        if (AbstractEntity.class.isAssignableFrom(javaType)) {
            return forEntityOpt((Class<? extends AbstractEntity<?>>) javaType);
        }
        return forComponent(javaType);
    }

    @Override
    public EntityMetadata forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return forEntityOpt(entityType)
                .orElseThrow(() -> new DomainMetadataGenerationException(ERR_ENTITY_TYPE_CANNOT_BE_SUBJECT_TO_METADATA_GENERATION.formatted(entityType.getTypeName())));
    }

    @Override
    public Optional<EntityMetadata> forEntityOpt(final Class<? extends AbstractEntity<?>> entityType) {
        return generator.forEntity(entityType);
    }

    @Override
    public Optional<TypeMetadata.Component> forComponent(final Class<?> javaType) {
        return generator.forComponent(javaType);
    }

    @Override
    public Optional<PropertyMetadata> forPropertyOpt(final Class<?> enclosingType, final CharSequence propPath) {
        return forType(enclosingType).flatMap(tm -> {
            final String[] names = splitPropPathToArray(propPath);
            // Optimise for the most common cases.
            return switch (names.length) {
                case 1 -> propertyFromType(tm, names[0]);
                case 2 -> propertyFromType(tm, names[0]).flatMap(pm0 -> forProperty_(pm0, names[1]));
                default -> {
                    var optPm = propertyFromType(tm, names[0]);
                    for (int i = 1; i < names.length; i++) {
                        if (optPm.isEmpty()) {
                            break;
                        }
                        final var name = names[i];
                        optPm = forProperty_(optPm.get(), name);
                    }
                    yield optPm;
                }
            };
        });
    }

    @Override
    public PropertyMetadata forProperty(final Class<?> enclosingType, final CharSequence propPath) {
        return forPropertyOpt(enclosingType, propPath)
                .orElseThrow(() -> noSuchPropertyException(enclosingType, propPath));
    }

    @Override
    public Either<RuntimeException, Optional<PropertyMetadata>> forProperty(final MetaProperty<?> metaProperty) {
        final var entityType = (Class<? extends AbstractEntity<?>>) metaProperty.getEntity().getClass();
        return forEntity(entityType).property(metaProperty);
    }

    private Optional<PropertyMetadata> forProperty_(final PropertyMetadata pm, final String propPath) {
        return switch (pm.type()) {
            case PropertyTypeMetadata.Component ct -> forPropertyOpt(ct.javaType(), propPath);
            case PropertyTypeMetadata.Entity et -> forPropertyOpt(et.javaType(), propPath);
            default -> Optional.empty();
        };
    }

    private Optional<PropertyMetadata> propertyFromType(final TypeMetadata tm, final String simpleProp) {
        return switch (tm) {
            case EntityMetadata em -> em.propertyOpt(simpleProp);
            case TypeMetadata.Component cm -> cm.propertyOpt(simpleProp);
        };
    }

}
