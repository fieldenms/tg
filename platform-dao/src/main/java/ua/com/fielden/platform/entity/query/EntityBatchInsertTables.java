package ua.com.fielden.platform.entity.query;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.types.tuples.T2.t2;

@Singleton
public final class EntityBatchInsertTables {

    private final Map<String, TableStructForBatchInsertion> tables;

    @Inject
    EntityBatchInsertTables(final IDomainMetadata domainMetadata, final IDomainMetadataUtils domainMetadataUtils) {
        tables = generateTables(domainMetadata, domainMetadataUtils).collect(toConcurrentMap(t2 -> t2._1, t2 -> t2._2));
    }

    public @Nullable TableStructForBatchInsertion getTableStructsForBatchInsertion(final Class<? extends AbstractEntity<?>> entityType) {
        return tables.get(entityType.getName());
    }

    private static Stream<T2<String, TableStructForBatchInsertion>> generateTables(
            final IDomainMetadata domainMetadata,
            final IDomainMetadataUtils domainMetadataUtils)
    {
        return domainMetadataUtils.registeredEntities()
                .parallel()
                .map(EntityMetadata::asPersistent).flatMap(Optional::stream)
                .map(em -> t2(em.javaType().getName(), generateTableStructForBatchInsertion(domainMetadata, em)));
    }

    private static TableStructForBatchInsertion generateTableStructForBatchInsertion(
            final IDomainMetadata domainMetadata,
            final EntityMetadata.Persistent entityMetadata)
    {
        // a way to do inner helper methods (avoids pollution of the outer class method namespace)
        class $ {
            static String mkColumnName(final PropertyMetadataUtils pmUtils, final PropertyMetadata prop) {
                return prop.name() + (pmUtils.isPropEntityType(prop, EntityMetadata::isPersistent) ? ("." + ID) : "");
            }
        }

        final var pmUtils = domainMetadata.propertyMetadataUtils();
        final var columns = entityMetadata.properties().stream()
                .filter(prop -> !ID.equals(prop.name()) && !VERSION.equals(prop.name()))
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .flatMap(prop -> {
                    if (prop.type().isComponent()) {
                        final var subColumnNames = pmUtils.subProperties(prop).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(p -> p.data().column().name)
                                .toList();
                        return subColumnNames.isEmpty()
                                ? Stream.of()
                                : Stream.of(new TableStructForBatchInsertion.PropColumnInfo(prop.name(), subColumnNames, prop.hibType()));
                    }
                    else if (pmUtils.isPropEntityType(prop, EntityMetadata::isUnion)) {
                        return pmUtils.subProperties(prop).stream()
                                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                                .map(subProp -> {
                                    final String colName = prop.name() + "." + $.mkColumnName(pmUtils, subProp);
                                    return new TableStructForBatchInsertion.PropColumnInfo(colName, subProp.data().column().name, subProp.hibType());
                                });
                    }
                    else {
                        return Stream.of(new TableStructForBatchInsertion.PropColumnInfo($.mkColumnName(pmUtils, prop), prop.data().column().name, prop.hibType()));
                    }
                })
                .toList();

        return new TableStructForBatchInsertion(entityMetadata.data().tableName(), columns);
    }

}
