package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

final class DomainMetadataImpl implements IDomainMetadata {

    /** Mutable map, may be populated with entity types for which metadata is generated ad-hoc. */
    private final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap;
    /** Mutable map, may be populated with composite types for which metadata is generated ad-hoc. */
    private final Map<Class<?>, TypeMetadata.Composite> compositeTypeMetadataMap;
    private final DomainMetadataGenerator generator;
    private final PropertyMetadataUtils pmUtils;
    private final EntityMetadataUtils emUtils;

    DomainMetadataImpl(final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap,
                       final Map<Class<?>, TypeMetadata.Composite> compositeTypeMetadataMap,
                       final DomainMetadataGenerator generator,
                       final DbVersion dbVersion) {
        this.entityMetadataMap = new ConcurrentHashMap<>(entityMetadataMap);
        this.compositeTypeMetadataMap = new ConcurrentHashMap<>(compositeTypeMetadataMap);
        this.generator = generator;
        this.pmUtils = new PropertyMetadataUtilsImpl(this, generator);
        this.emUtils = new EntityMetadataUtilsImpl();

        this.dbVersion = dbVersion;
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
        return Stream.concat(entityMetadataMap.values().stream(), compositeTypeMetadataMap.values().stream());
    }

    @Override
    public <T extends TypeMetadata> Stream<T> allTypes(final Class<T> metadataType) {
        if (metadataType == EntityMetadata.class) {
            return (Stream<T>) entityMetadataMap.values().stream();
        }
        else if (metadataType == TypeMetadata.Composite.class) {
            return (Stream<T>) compositeTypeMetadataMap.values().stream();
        }
        else {
            return entityMetadataMap.values().stream()
                    .mapMulti(typeFilter(metadataType));
        }
    }

    @Override
    public Optional<? extends TypeMetadata> forType(final Class<?> javaType) {
        if (AbstractEntity.class.isAssignableFrom(javaType)) {
            return forEntityOpt((Class<? extends AbstractEntity<?>>) javaType);
        }
        return forComposite(javaType);
    }

    @Override
    public EntityMetadata forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return forEntityOpt(entityType)
                .orElseThrow(() -> new DomainMetadataGenerationException(
                        format("Could not generate metadata for entity [%s]", entityType.getTypeName())));
    }

    @Override
    public Optional<EntityMetadata> forEntityOpt(final Class<? extends AbstractEntity<?>> entityType) {
        final var em = entityMetadataMap.get(entityType);
        if (em != null) {
            return Optional.of(em);
        }

        // TODO cache?
        return generator.forEntity(entityType);
    }

    @Override
    public Optional<TypeMetadata.Composite> forComposite(final Class<?> javaType) {
        return Optional.ofNullable(compositeTypeMetadataMap.get(javaType))
                .or(() -> generator.forComposite(javaType));
    }

    @Override
    public Optional<PropertyMetadata> forProperty(final Class<?> enclosingType, final CharSequence propPath) {
        return forType(enclosingType).flatMap(tm -> {
            final Pair<String, String> head_tail = EntityUtils.splitPropByFirstDot(propPath.toString());
            final var optHeadPm = propertyFromType(tm, head_tail.getKey());
            final @Nullable String tail = head_tail.getValue();
            return tail == null ? optHeadPm : optHeadPm.flatMap(h -> forProperty_(h, tail));
        });
    }

    private Optional<PropertyMetadata> forProperty_(final PropertyMetadata pm, final String propPath) {
        return switch (pm.type()) {
            case PropertyTypeMetadata.Composite ct -> forProperty(ct.javaType(), propPath);
            case PropertyTypeMetadata.Entity et -> forProperty(et.javaType(), propPath);
            default -> Optional.empty();
        };
    }

    private Optional<PropertyMetadata> propertyFromType(final TypeMetadata tm, final String simpleProp) {
        return switch (tm) {
            case EntityMetadata em -> em.property(simpleProp);
            case TypeMetadata.Composite cm -> cm.property(simpleProp);
        };
    }

    // ****************************************
    // * Temporary baggage from old metadata that can't be moved until dependency injection is properly configured.

    private final DbVersion dbVersion;

    @Override
    public DbVersion dbVersion() {
        return dbVersion;
    }
}
