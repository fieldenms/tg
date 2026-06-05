package ua.com.fielden.platform.ddl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.dbschema.ColumnDefinitionExtractor;
import ua.com.fielden.platform.eql.dbschema.TableDdl;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

class DdlGeneratorImpl implements IDdlGenerator {
    private final HibernateTypeMappings hibernateTypeMappings;
    private final Supplier<Collection<Class<? extends AbstractEntity<?>>>> entityTypes;

    @Inject
    protected DdlGeneratorImpl(
            final HibernateTypeMappings hibernateTypeMappings,
            final Provider<IApplicationDomainProvider> appDomainProvider)
    {
        this(hibernateTypeMappings, () -> appDomainProvider.get().entityTypes());
    }

    DdlGeneratorImpl(final HibernateTypeMappings hibernateTypeMappings,
                     final Supplier<Collection<Class<? extends AbstractEntity<?>>>> entityTypes)
    {
        this.hibernateTypeMappings = hibernateTypeMappings;
        this.entityTypes = entityTypes;
    }

    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect, final boolean withFk) {
        return generatePhasedDatabaseDdl_(dialect, withFk, entityTypes.get().stream()).flattenWithMarkers();
    }

    @Override
    public List<String> generateDatabaseDdl(
            final Dialect dialect,
            final boolean withFk,
            final Class<? extends AbstractEntity<?>> type,
            final Class<? extends AbstractEntity<?>>... types)
    {
        return generatePhasedDatabaseDdl_(dialect, withFk, Stream.concat(Stream.of(type), Arrays.stream(types))).flattenWithMarkers();
    }

    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect, final Collection<Class<? extends AbstractEntity<?>>> types) {
        return generatePhasedDatabaseDdl_(dialect, true, types.stream()).flattenWithMarkers();
    }

    @Override
    public List<String> generateDatabaseDdl(
            final Dialect dialect,
            final boolean withFk,
            final Collection<Class<? extends AbstractEntity<?>>> types)
    {
        return generatePhasedDatabaseDdl_(dialect, withFk, types.stream()).flattenWithMarkers();
    }

    @Override
    public PhasedDdl generatePhasedDatabaseDdl(final Dialect dialect, final boolean withFk) {
        return generatePhasedDatabaseDdl_(dialect, withFk, entityTypes.get().stream());
    }

    @Override
    public PhasedDdl generatePhasedDatabaseDdl(
            final Dialect dialect,
            final boolean withFk,
            final Collection<Class<? extends AbstractEntity<?>>> types)
    {
        return generatePhasedDatabaseDdl_(dialect, withFk, types.stream());
    }

    private PhasedDdl generatePhasedDatabaseDdl_(final Dialect dialect, final boolean withFk, final Stream<? extends Class<? extends AbstractEntity<?>>> types) {
        final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor(hibernateTypeMappings, dialect);

        final Set<String> ddlTables = new LinkedHashSet<>();
        final Set<String> ddlIndices = new LinkedHashSet<>();
        final Set<String> ddlFKs = new LinkedHashSet<>();
        types.filter(EntityUtils::isPersistentEntityType).forEach(entityType -> {
            final TableDdl tableDefinition = new TableDdl(columnDefinitionExtractor, entityType);
            ddlTables.add(tableDefinition.createTableSchema(dialect));
            ddlTables.add(tableDefinition.createPkSchema(dialect));
            ddlIndices.addAll(tableDefinition.createIndicesSchema(dialect));
            if (withFk) {
                ddlFKs.addAll(tableDefinition.createFkSchema(dialect));
            }
        });

        return new PhasedDdl(
                ddlTables.stream().collect(toImmutableList()),
                ddlIndices.stream().collect(toImmutableList()),
                ddlFKs.stream().collect(toImmutableList()));
    }
}
