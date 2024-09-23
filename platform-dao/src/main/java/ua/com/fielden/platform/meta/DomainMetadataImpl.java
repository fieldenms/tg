package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;

/**
 * The default implementation of {@link IDomainMetadata}.
 */
final class DomainMetadataImpl implements IDomainMetadata {

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
                .orElseThrow(() -> new DomainMetadataGenerationException("Could not generate metadata for entity [%s].".formatted(entityType.getTypeName())));
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
            final Pair<String, String> head_tail = EntityUtils.splitPropByFirstDot(propPath.toString());
            final var optHeadPm = propertyFromType(tm, head_tail.getKey());
            final @Nullable String tail = head_tail.getValue();
            return tail == null ? optHeadPm : optHeadPm.flatMap(h -> forProperty_(h, tail));
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
