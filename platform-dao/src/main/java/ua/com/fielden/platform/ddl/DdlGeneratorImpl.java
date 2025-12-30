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
            final Supplier<Collection<Class<? extends AbstractEntity<?>>>> entityTypes) {
        this.hibernateTypeMappings = hibernateTypeMappings;
        this.entityTypes = entityTypes;
    }

    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect, final boolean withFk) {
        return generateDatabaseDdl_(dialect, withFk, entityTypes.get().stream());
    }

    @Override
    public List<String> generateDatabaseDdl(
            final Dialect dialect,
            final boolean withFk,
            final Class<? extends AbstractEntity<?>> type,
            final Class<? extends AbstractEntity<?>>... types)
    {
        return generateDatabaseDdl_(dialect, withFk, Stream.concat(Stream.of(type), Arrays.stream(types)));
    }

    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect, final Collection<Class<? extends AbstractEntity<?>>> types) {
        return generateDatabaseDdl_(dialect, true, types.stream());
    }

    @Override
    public List<String> generateDatabaseDdl(
            final Dialect dialect,
            final boolean withFk,
            final Collection<Class<? extends AbstractEntity<?>>> types)
    {
        return generateDatabaseDdl_(dialect, withFk, types.stream());
    }

    private List<String> generateDatabaseDdl_(final Dialect dialect, final boolean withFk, final Stream<? extends Class<? extends AbstractEntity<?>>> types) {
        final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor(hibernateTypeMappings, dialect);

        final Set<String> ddlTables = new LinkedHashSet<>();
        final Set<String> ddlFKs = new LinkedHashSet<>();
        types.filter(EntityUtils::isPersistentEntityType).forEach(entityType -> {
            final TableDdl tableDefinition = new TableDdl(columnDefinitionExtractor, entityType);
            ddlTables.add(tableDefinition.createTableSchema(dialect));
            ddlTables.add(tableDefinition.createPkSchema(dialect));
            ddlTables.addAll(tableDefinition.createIndicesSchema(dialect));
            if (withFk) {
                ddlFKs.addAll(tableDefinition.createFkSchema(dialect));
            }
        });

        return Stream.concat(ddlTables.stream(), ddlFKs.stream())
                .collect(toImmutableList());
    }
}
