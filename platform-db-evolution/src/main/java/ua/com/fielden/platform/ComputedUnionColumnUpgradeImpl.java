package ua.com.fielden.platform;

import com.google.common.collect.Streams;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.IComputedUnionColumnUpgrade.Problem.MISSING;
import static ua.com.fielden.platform.IComputedUnionColumnUpgrade.Problem.OUTDATED;
import static ua.com.fielden.platform.eql.dbschema.ColumnDefinitionExtractor.mkColumnName;
import static ua.com.fielden.platform.eql.dbschema.TableDdl.*;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

class ComputedUnionColumnUpgradeImpl implements IComputedUnionColumnUpgrade {

    private static final String
            ERR_NOT_UNION_TYPED = "The type of [%s.%s] is not a union entity.",
            ERR_UNSUPPORTED_DB = "Unsupported DB: [%s].",
            ERR_CATALOG_QUERY = "Could not read the database catalog for [%s.%s].",
            ERR_MISSING_COMPUTED_COLUMN = "Computed column [%s.%s] does not exist.",
            ERR_MISSING_INDEX = "Index [%s] does not exist.",
            ERR_OUTDATED_MEMBERS = "The expression of [%s] lists union member columns %s instead of %s.",
            ERR_OUTDATED_INDEX_COLUMN = "Index [%s] has key columns %s instead of [%s ASC].",
            ERR_OUTDATED_INDEX_TABLE = "Index [%s] is on table [%s] instead of [%s].",
            ERR_OUTDATED_INDEX_ORDER = "Index [%s] is descending instead of ascending.";

    /// Matches a branch of the union expression in SQL Server's definition of a computed column,
    /// e.g., `when [LOCATION__WAGONSLOT] IS NOT NULL then [LOCATION__WAGONSLOT]`.
    ///
    private static final Pattern MSSQL_WHEN = Pattern.compile("when\\s+\\[([^\\]]+)\\]\\s+is\\s+not\\s+null\\s+then\\s+\\[([^\\]]+)\\]", CASE_INSENSITIVE);

    /// Matches a branch of the union expression in PostgreSQL's definition of an index,
    /// e.g., `WHEN (location__wagonslot IS NOT NULL) THEN location__wagonslot`.
    ///
    private static final Pattern PG_WHEN = Pattern.compile("when\\s+\\(\"?(\\w+)\"?\\s+is\\s+not\\s+null\\)\\s+then\\s+\"?(\\w+)\"?", CASE_INSENSITIVE);

    /// Matches the end of PostgreSQL's definition of a descending index on an expression, e.g., `... END) DESC)`.
    ///
    private static final Pattern PG_DESC = Pattern.compile("\\)\\s+desc(\\s+nulls\\s+\\w+)?\\)\\s*$", CASE_INSENSITIVE);

    private final DbVersion dbVersion;
    private final EqlTables eqlTables;
    private final IDomainMetadata domainMetadata;
    private final IApplicationDomainProvider appDomainProvider;
    private final Provider<TransactionalExecution> transactionalExecutionProvider;

    @Inject
    protected ComputedUnionColumnUpgradeImpl(
            final IDbVersionProvider dbVersionProvider,
            final EqlTables eqlTables,
            final IDomainMetadata domainMetadata,
            final IApplicationDomainProvider appDomainProvider,
            final Provider<TransactionalExecution> transactionalExecutionProvider)
    {
        this.dbVersion = dbVersionProvider.dbVersion();
        this.eqlTables = eqlTables;
        this.domainMetadata = domainMetadata;
        this.appDomainProvider = appDomainProvider;
        this.transactionalExecutionProvider = transactionalExecutionProvider;
    }

    @Override
    public UnionPropertySelector<String> createSql() {
        return select(properties -> properties.map(this::createSql).collect(joining("\n")));
    }

    @Override
    public UnionPropertySelector<String> dropSql() {
        return select(properties -> properties.map(this::dropSql).collect(joining("\n")));
    }

    @Override
    public List<OutdatedUnionIndex> findOutdated() {
        final var props = unionProperties(appDomainProvider.entityTypes()).toList();
        return transactionalExecutionProvider.get().execFn(conn -> props.stream()
                .flatMap(prop -> findProblem(conn, prop).stream())
                .toList());
    }

    /// Returns a selector that applies `action` to the selected properties.
    ///
    private <T> UnionPropertySelector<T> select(final Function<Stream<UnionProperty>, T> action) {
        return new UnionPropertySelector<>() {
            @Override
            public T allInDomain() {
                return propertiesOf(appDomainProvider.entityTypes());
            }

            @Override
            public T property(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
                return action.apply(Stream.of(unionProperty(entityType, property.toString())));
            }

            @Override
            public T propertiesOf(final Iterable<Class<? extends AbstractEntity<?>>> entityTypes) {
                return action.apply(unionProperties(entityTypes));
            }

            @Override
            public T propertiesWithType(final Class<? extends AbstractUnionEntity> unionType) {
                return action.apply(unionProperties(appDomainProvider.entityTypes()).filter(prop -> prop.unionType().equals(unionType)));
            }
        };
    }

    /// A union-typed persistent property, together with the names of the database objects that the current DDL defines for it.
    ///
    private record UnionProperty(
            Class<? extends AbstractEntity<?>> entityType,
            String property,
            Class<? extends AbstractUnionEntity> unionType,
            String tableName,
            String columnName,
            String indexName)
    {
        List<String> memberColumns() {
            return unionMemberColumns(unionType, columnName);
        }

        String expression() {
            return mkUnionExprSql(unionType, columnName);
        }
    }

    private Stream<UnionProperty> unionProperties(final Iterable<Class<? extends AbstractEntity<?>>> entityTypes) {
        return Streams.stream(entityTypes)
                .flatMap(entityType -> domainMetadata.forEntityOpt(entityType).flatMap(EntityMetadata::asPersistent).stream())
                .flatMap(em -> em.properties().stream()
                                 .flatMap(prop -> prop.asPersistent().stream())
                                 .filter(prop -> domainMetadata.propertyMetadataUtils().isPropEntityType(prop, EntityMetadata::isUnion))
                                 .map(prop -> unionProperty(em.javaType(), prop.name())));
    }

    private UnionProperty unionProperty(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        final var unionType = asUnion(domainMetadata.forProperty(entityType, property).type().javaType())
                .orElseThrow(() -> new InvalidArgumentException(ERR_NOT_UNION_TYPED.formatted(entityType.getName(), property)));
        final var tableName = eqlTables.getTableForEntityTypeOrThrow(entityType).name();
        final var columnName = mkColumnName(entityType, property);
        return new UnionProperty(entityType, property, unionType, tableName, columnName, indexName(tableName, columnName));
    }

    private String createSql(final UnionProperty prop) {
        return switch (dbVersion) {
            case MSSQL -> """
                          IF COL_LENGTH('%1$s', '%2$s') IS NULL
                              ALTER TABLE %1$s ADD %2$s AS (%3$s);
                          IF NOT EXISTS (SELECT 1 FROM sys.indexes
                                         WHERE object_id = OBJECT_ID('%1$s') AND name = '%4$s')
                              CREATE INDEX %4$s ON %1$s(%2$s ASC);
                          """.formatted(prop.tableName(), prop.columnName(), prop.expression(), prop.indexName());
            case POSTGRESQL -> "CREATE INDEX IF NOT EXISTS %s ON %s ((%s) ASC);".formatted(prop.indexName(), prop.tableName(), prop.expression());
            default -> throw new DbSchemaException(ERR_UNSUPPORTED_DB.formatted(dbVersion));
        };
    }

    private String dropSql(final UnionProperty prop) {
        return switch (dbVersion) {
            case MSSQL -> """
                          IF EXISTS (SELECT 1 FROM sys.indexes
                                     WHERE object_id = OBJECT_ID('%1$s') AND name = '%3$s')
                              DROP INDEX %3$s ON %1$s;
                          IF COL_LENGTH('%1$s', '%2$s') IS NOT NULL
                              ALTER TABLE %1$s DROP COLUMN %2$s;
                          """.formatted(prop.tableName(), prop.columnName(), prop.indexName());
            case POSTGRESQL -> "DROP INDEX IF EXISTS %s;".formatted(prop.indexName());
            default -> throw new DbSchemaException(ERR_UNSUPPORTED_DB.formatted(dbVersion));
        };
    }

    /// Compares the database objects for `prop` with the current DDL, returning the first difference found.
    ///
    private Optional<OutdatedUnionIndex> findProblem(final Connection conn, final UnionProperty prop) {
        try {
            return switch (dbVersion) {
                case MSSQL -> findProblemMssql(conn, prop);
                case POSTGRESQL -> findProblemPostgresql(conn, prop);
                default -> throw new DbSchemaException(ERR_UNSUPPORTED_DB.formatted(dbVersion));
            };
        } catch (final SQLException ex) {
            throw new DbSchemaException(ERR_CATALOG_QUERY.formatted(prop.entityType().getSimpleName(), prop.property()), ex);
        }
    }

    private Optional<OutdatedUnionIndex> findProblemMssql(final Connection conn, final UnionProperty prop) throws SQLException {
        final var definitions = query(conn, "SELECT definition FROM sys.computed_columns WHERE object_id = OBJECT_ID(?) AND name = ?",
                                      prop.tableName(), prop.columnName());
        if (definitions.isEmpty()) {
            return problem(prop, MISSING, ERR_MISSING_COMPUTED_COLUMN.formatted(prop.tableName(), prop.columnName()));
        }
        final var memberColumns = memberColumns(MSSQL_WHEN, definitions.getFirst().getFirst());
        if (!memberColumns.equals(upperCase(prop.memberColumns()))) {
            return problem(prop, OUTDATED, ERR_OUTDATED_MEMBERS.formatted(prop.columnName(), memberColumns, upperCase(prop.memberColumns())));
        }

        final var keyColumns = query(conn, """
                                           SELECT c.name, CAST(ic.is_descending_key AS int)
                                           FROM sys.indexes i
                                           JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.key_ordinal > 0
                                           JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
                                           WHERE i.object_id = OBJECT_ID(?) AND i.name = ?
                                           ORDER BY ic.key_ordinal
                                           """, prop.tableName(), prop.indexName());
        if (keyColumns.isEmpty()) {
            return problem(prop, MISSING, ERR_MISSING_INDEX.formatted(prop.indexName()));
        }
        final var expectedKeyColumn = List.of(prop.columnName(), "0");
        if (keyColumns.size() != 1 || !upperCase(keyColumns.getFirst()).equals(upperCase(expectedKeyColumn))) {
            return problem(prop, OUTDATED, ERR_OUTDATED_INDEX_COLUMN.formatted(prop.indexName(), keyColumns, prop.columnName()));
        }
        return Optional.empty();
    }

    private Optional<OutdatedUnionIndex> findProblemPostgresql(final Connection conn, final UnionProperty prop) throws SQLException {
        // Unquoted identifiers are stored in lower case.
        final var indexes = query(conn, "SELECT tablename, indexdef FROM pg_indexes WHERE schemaname = current_schema() AND indexname = ?",
                                  prop.indexName().toLowerCase(ROOT));
        if (indexes.isEmpty()) {
            return problem(prop, MISSING, ERR_MISSING_INDEX.formatted(prop.indexName()));
        }
        final var tableName = indexes.getFirst().get(0);
        final var definition = indexes.getFirst().get(1);
        if (!tableName.equalsIgnoreCase(prop.tableName())) {
            return problem(prop, OUTDATED, ERR_OUTDATED_INDEX_TABLE.formatted(prop.indexName(), tableName, prop.tableName()));
        }
        final var memberColumns = memberColumns(PG_WHEN, definition);
        if (!memberColumns.equals(upperCase(prop.memberColumns()))) {
            return problem(prop, OUTDATED, ERR_OUTDATED_MEMBERS.formatted(prop.indexName(), memberColumns, upperCase(prop.memberColumns())));
        }
        if (PG_DESC.matcher(definition).find()) {
            return problem(prop, OUTDATED, ERR_OUTDATED_INDEX_ORDER.formatted(prop.indexName()));
        }
        return Optional.empty();
    }

    /// Returns the member columns listed by the branches of a union expression, in upper case.
    /// A branch that tests one column and yields another is represented by both columns, separated by `/`, which never matches the expected list.
    ///
    private static List<String> memberColumns(final Pattern when, final String definition) {
        return when.matcher(definition).results()
                .map(match -> {
                    final var tested = match.group(1).toUpperCase(ROOT);
                    final var yielded = match.group(2).toUpperCase(ROOT);
                    return tested.equals(yielded) ? tested : tested + "/" + yielded;
                })
                .toList();
    }

    private static List<String> upperCase(final List<String> values) {
        return values.stream().map(value -> value.toUpperCase(ROOT)).toList();
    }

    private static Optional<OutdatedUnionIndex> problem(final UnionProperty prop, final Problem problem, final String details) {
        return Optional.of(new OutdatedUnionIndex(prop.entityType(), prop.property(), prop.unionType(), problem, details));
    }

    /// Executes a parameterised query, returning the values of all columns of each row.
    ///
    private static List<List<String>> query(final Connection conn, final String sql, final String... params) throws SQLException {
        try (final var stmt = conn.prepareStatement(sql)) {
            for (int index = 0; index < params.length; index++) {
                stmt.setString(index + 1, params[index]);
            }
            try (final var rs = stmt.executeQuery()) {
                final var columnCount = rs.getMetaData().getColumnCount();
                final var rows = new ArrayList<List<String>>();
                while (rs.next()) {
                    final var row = new ArrayList<String>(columnCount);
                    for (int column = 1; column <= columnCount; column++) {
                        row.add(rs.getString(column));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends AbstractUnionEntity>> asUnion(final Class<?> type) {
        return !isUnionEntityType(type) ? Optional.empty() : Optional.of((Class<? extends AbstractUnionEntity>) type);
    }

}
