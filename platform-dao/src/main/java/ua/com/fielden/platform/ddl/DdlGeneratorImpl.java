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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

class DdlGeneratorImpl implements IDdlGenerator {

    private final HibernateTypeMappings hibernateTypeMappings;
    private final Supplier<Collection<Class<? extends AbstractEntity<?>>>> entityTypes;

    @Inject
    DdlGeneratorImpl(final HibernateTypeMappings hibernateTypeMappings,
                     final Provider<IApplicationDomainProvider> appDomainProvider) {
        this(hibernateTypeMappings, () -> appDomainProvider.get().entityTypes());
    }

    DdlGeneratorImpl(final HibernateTypeMappings hibernateTypeMappings,
                     final Supplier<Collection<Class<? extends AbstractEntity<?>>>> entityTypes) {
        this.hibernateTypeMappings = hibernateTypeMappings;
        this.entityTypes = entityTypes;
    }

    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect) {
        return generateDatabaseDdl_(dialect, entityTypes.get().stream());
    }

    @Override
    public List<String> generateDatabaseDdl(final Dialect dialect, final Collection<Class<? extends AbstractEntity<?>>> types) {
        return generateDatabaseDdl_(dialect, types.stream());
    }

    private List<String> generateDatabaseDdl_(final Dialect dialect, final Stream<? extends Class<? extends AbstractEntity<?>>> types) {
        final ColumnDefinitionExtractor columnDefinitionExtractor = new ColumnDefinitionExtractor(hibernateTypeMappings, dialect);

        final Set<String> ddlTables = new LinkedHashSet<>();
        final Set<String> ddlFKs = new LinkedHashSet<>();

        types.filter(EntityUtils::isPersistedEntityType).forEach(entityType -> {
            final TableDdl tableDefinition = new TableDdl(columnDefinitionExtractor, entityType);
            ddlTables.add(tableDefinition.createTableSchema(dialect, ""));
            ddlTables.add(tableDefinition.createPkSchema(dialect));
            ddlTables.addAll(tableDefinition.createIndicesSchema(dialect));
            ddlFKs.addAll(tableDefinition.createFkSchema(dialect));
        });

        return Stream.concat(ddlTables.stream(), ddlFKs.stream())
                .collect(toImmutableList());
    }

}
