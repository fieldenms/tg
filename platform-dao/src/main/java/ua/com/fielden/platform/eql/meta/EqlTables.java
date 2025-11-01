package ua.com.fielden.platform.eql.meta;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;

@Singleton
public class EqlTables {

    private final Map<Class<? extends AbstractEntity<?>>, EqlTable> tables;

    @Inject
    public EqlTables(final IDomainMetadata domainMetadata, final IDomainMetadataUtils domainMetadataUtils) {
        final var pmUtils = domainMetadata.propertyMetadataUtils();
        tables = domainMetadataUtils.registeredEntities()
                .parallel()
                .map(EntityMetadata::asPersistent).flatMap(Optional::stream)
                .collect(toConcurrentMap(EntityMetadata::javaType, em -> generateEqlTable(pmUtils, em)));
    }

    public @Nullable EqlTable getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return tables.get(DynamicEntityClassLoader.getOriginalType(entityType));
    }

    private static EqlTable generateEqlTable(final PropertyMetadataUtils pmUtils, final EntityMetadata.Persistent entityMetadata) {
        final Map<String, String> columns = entityMetadata.properties().stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .flatMap(prop -> {
                    if (prop.type().isComponent() || pmUtils.isPropEntityType(prop, EntityMetadata::isUnion)) {
                        return pmUtils.subProperties(prop).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(subProp -> t2(prop.name() + "." + subProp.name(), subProp.data().column().name));
                    } else {
                        return Stream.of(t2(prop.name(), prop.data().column().name));
                    }
                })
                .collect(toConcurrentMap(t2 -> t2._1, t2 -> t2._2));

        return new EqlTable(entityMetadata.data().tableName(), columns);
    }

}
