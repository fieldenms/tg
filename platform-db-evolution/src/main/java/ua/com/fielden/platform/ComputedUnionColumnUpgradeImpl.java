package ua.com.fielden.platform;

import com.google.common.collect.Streams;
import jakarta.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.dbschema.ColumnDefinitionExtractor.mkColumnName;
import static ua.com.fielden.platform.eql.dbschema.TableDdl.indexName;
import static ua.com.fielden.platform.eql.dbschema.TableDdl.mkUnionExprSql;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

class ComputedUnionColumnUpgradeImpl implements IComputedUnionColumnUpgrade {

    private final DbVersion dbVersion;
    private final EqlTables eqlTables;
    private final IDomainMetadata domainMetadata;
    private final IApplicationDomainProvider appDomainProvider;

    @Inject
    protected ComputedUnionColumnUpgradeImpl(
            final IDbVersionProvider dbVersionProvider,
            final EqlTables eqlTables,
            final IDomainMetadata domainMetadata,
            final IApplicationDomainProvider appDomainProvider)
    {
        this.dbVersion = dbVersionProvider.dbVersion();
        this.eqlTables = eqlTables;
        this.domainMetadata = domainMetadata;
        this.appDomainProvider = appDomainProvider;
    }

    @Override
    public String sql() {
        return sql(appDomainProvider.entityTypes());
    }

    @Override
    public String sql(final Iterable<Class<? extends AbstractEntity<?>>> entityTypes) {
        return Streams.stream(entityTypes)
                .flatMap(entityTy -> domainMetadata.forEntityOpt(entityTy).flatMap(EntityMetadata::asPersistent).stream())
                .flatMap(em -> em.properties().stream()
                                 .flatMap(prop -> prop.asPersistent().stream())
                                 .filter(prop -> domainMetadata.propertyMetadataUtils().isPropEntityType(prop, EntityMetadata::isUnion))
                                 .map(prop -> sql(em.javaType(), prop.name())))
                .collect(joining("\n"));
    }

    @Override
    public String sql(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        final var unionType = asUnion(domainMetadata.forProperty(entityType, property).type().javaType())
                .orElseThrow(() -> new InvalidArgumentException("The type of [%s.%s] is not a union entity.".formatted(entityType.getName(), property)));

        final var tableName = eqlTables.getTableForEntityTypeOrThrow(entityType).name();
        final var computedColumnName = mkColumnName(entityType, property);
        final var indexName = indexName(tableName, computedColumnName);
        final var exprSql = mkUnionExprSql(unionType, computedColumnName);
        final var computedColumnDef = "%s AS (%s)".formatted(computedColumnName, exprSql);

        return switch (dbVersion) {
            case MSSQL -> """
                          IF COL_LENGTH('%1$s', '%2$s') IS NULL
                              ALTER TABLE %1$s ADD %3$s;
                          """.formatted(tableName, computedColumnName, computedColumnDef)
                          +
                          """
                          IF NOT EXISTS (SELECT 1 FROM sys.indexes
                                         WHERE object_id = OBJECT_ID('%1$s') AND name = '%2$s')
                              CREATE INDEX %2$s ON %1$s(%3$s ASC);
                          """.formatted(tableName, indexName, computedColumnName);
            case POSTGRESQL -> "CREATE INDEX IF NOT EXISTS %s ON %s ((%s) ASC);".formatted(indexName, tableName, exprSql);
            default -> throw new DbSchemaException("Unsupported DB: [%s].".formatted(dbVersion));
        };
    }

    @SuppressWarnings("unchecked")
    private Optional<Class<? extends AbstractUnionEntity>> asUnion(final Class<?> type) {
        return !isUnionEntityType(type) ? Optional.empty() : Optional.of((Class<? extends AbstractUnionEntity>) type);
    }

}
