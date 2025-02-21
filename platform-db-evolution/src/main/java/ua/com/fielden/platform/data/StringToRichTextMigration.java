package ua.com.fielden.platform.data;

import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.LiteralType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.eql.dbschema.ColumnDefinition;
import ua.com.fielden.platform.eql.dbschema.ColumnDefinitionExtractor;
import ua.com.fielden.platform.eql.dbschema.TableDdl;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.IDates;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.CREATED_DATE;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_DATE;
import static ua.com.fielden.platform.entity.query.DbVersion.Nullability.NOT_NULL;
import static ua.com.fielden.platform.entity.query.DbVersion.Nullability.NULL;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;

/**
 * A utility to assist with migration of data for properties that change type from {@link String} to {@link RichText}.
 * It generates an SQL script that will perform the migration, given an entity type and a property that needs to be migrated.
 * <p>
 * There are 2 kinds of migrations that are supported:
 * <ul>
 *   <li> <i>Property type migration</i> - when some property X changes type from String to RichText.
 *        <p>In this case, the entity type provided to this utility must already have been modified, i.e., the property type must be RichText.
 *   <li> <i>Property-to-property migration</i> - when some String property X is replaced by some RichText property Y.
 *           (Properties X and Y have distinct names.)
 *        <p>In this case, the entity type provided to this utility must contain both properties.
 * </ul>
 * <p>
 * This utility follows the blue-green deployment strategy, and generates 3 SQL scripts.
 * <p>
 * For some fixed entity type T, a String property X and a RichText property Y, the following SQL scripts are generated:
 * <ol>
*   <li> <b>Initial migration script</b>.
 *   <ol>
 *     <li> Add a column for each component of Y. Any requiredness constraints are ignored at this stage ({@code NOT NULL} constraints are not generated).
 *     <li> Remove the {@code NOT NULL} constraint of X, if it has one.
 *     <li> Populate columns for Y from values in the column for X.
 *   </ol>
 *   <li> <b>Complete migration script</b>, which is expected to be used if the initial migration is successful.
 *   <ol>
 *     <li> Remove the column for X.
 *     <li> If Y has a requiredness constraint, add the {@code NOT NULL} constraint to its columns.
 *   </ol>
 *   <li> <b>Rollback script</b>, which is expected to be used if the initial migration is unsuccessful.
 *   <ol>
 *     <li> For each new or modified value of Y, populate the column for X from the column for the core text component of Y.
 *          New and modified values of Y can be identified using the "last updated by" property of T: if a "last updated by"
 *          date is later than the date of the execution of the initial migration script, then values of Y should be considered
 *          newer than values of X.
 *     <li> If X had a requiredness constraint, add the {@code NOT NULL} constraint to its column.
 *   </ol>
 * </ol>
 * <p>
 * Generated SQL scripts may require manual adjustment by the developer.
 * For this purpose, <i>placeholders</i> are used.
 * A placeholder is a string enclosed in angle brackets (e.g., {@code <THING>}) that needs to be replaced by a suitable value.
 * A description of each possibly appearing placeholder is included in the generated scripts.
 */
public final class StringToRichTextMigration {

    private final HibernateTypeMappings hibernateTypeMappings;
    private final DbVersion dbVersion;
    private final IDomainMetadata domainMetadata;
    private final Dialect dialect;
    private final IDates dates;
    private final LiteralType<Date> hibernateDateType;

    @Inject
    StringToRichTextMigration(
            final HibernateTypeMappings hibernateTypeMappings,
            final IDbVersionProvider dbVersionProvider,
            final IDomainMetadata domainMetadata,
            final IDates dates)
    {
        this.hibernateTypeMappings = hibernateTypeMappings;
        this.dbVersion = dbVersionProvider.dbVersion();
        this.domainMetadata = domainMetadata;
        this.dates = dates;
        this.dialect = HibernateHelpers.getDialect(dbVersion);
        this.hibernateDateType = (LiteralType<Date>) hibernateTypeMappings.getHibernateType(Date.class)
                .orElseThrow(() -> new IllegalStateException("Missing Hibernate type for type [%s]".formatted(Date.class.getTypeName())));
    }

    /**
     * Use this method when a property changes type from String to RichText.
     * <p>
     * For example, {@code desc : String} becomes {@code richDesc : RichText}.
     */
    public Tool migratePropertyType(final Class<? extends AbstractEntity<?>> entityType, final String property) {
        return new ToolImpl(entityType, property, property);
    }

    /**
     * Use this method when a String property is being replaced by a distinct RichText property.
     * <p>
     * For example, {@code desc : String} is replaced by {@code richDesc : RichText}.
     * <p>
     * Both properties must be persistent at the time of executing this method.
     */
    public Tool migrateToNewProperty(final Class<? extends AbstractEntity<?>> entityType, final String stringProperty, final String richTextProperty) {
        return new ToolImpl(entityType, stringProperty, richTextProperty);
    }

    public interface Tool {
        /**
         * Returns SQL scripts consisting of statements generated by {@link #generateSqlStatements(Consumer)}.
         *
         * @see #generateSqlStatements(Consumer)
         */
        String generateSql();

        /**
         * Generates SQL scripts that performs data migration from a String property to a {@link RichText} property.
         *
         * @param sqlConsumer  consumer of generated SQL statements, which may not necessarily be terminated with a semicolon
         */
        void generateSqlStatements(Consumer<? super String> sqlConsumer);
    }

    private final class ToolImpl implements Tool {

        private static final String IMPORTANT_MESSAGE = """
        ********** IMPORTANT **********
        * Make sure to address warnings that may have been printed above!
        *******************************
        """;

        private final Class<? extends AbstractEntity<?>> entityType;
        private final Optional<PropertyMetadata.Persistent> maybeStringPropertyMetadata;
        private final PropertyMetadata.Persistent richTextPropertyMetadata;
        private final String stringColumnName;
        private final TableDdl tableDdl;
        private final ColumnDefinition formattedTextColDef;
        private final ColumnDefinition coreTextColDef;
        private final String stringColumnTypeName;
        private final String batchDelimiter;

        private ToolImpl(
                final Class<? extends AbstractEntity<?>> entityType,
                final String stringProperty,
                final String richTextProperty)
        {
            this.entityType = entityType;

            final boolean sameProperty = stringProperty.equals(richTextProperty);
            maybeStringPropertyMetadata = sameProperty
                    ? Optional.empty()
                    : Optional.of(domainMetadata.forProperty(entityType, stringProperty)
                                          .asPersistent()
                                          .orElseThrow(() -> new InvalidArgumentException(
                                                  format("Expected property [%s.%s] to be persistent.",
                                                         entityType.getSimpleName(),
                                                         stringProperty))));
            richTextPropertyMetadata = domainMetadata.forProperty(entityType, richTextProperty)
                    .asPersistent()
                    .orElseThrow(() -> new InvalidArgumentException(
                            format("Expected property [%s.%s] to be persistent.", entityType.getSimpleName(),
                                   richTextProperty)));
            if (richTextPropertyMetadata.type().javaType() != RichText.class) {
                throw new InvalidArgumentException(
                        format("Expected property [%s.%s] to have type [%s]", entityType.getSimpleName(),
                               richTextProperty, RichText.class.getSimpleName()));
            }

            stringColumnName = maybeStringPropertyMetadata.map(it -> it.data().column().name)
                    .orElse(Placeholder.STRING_COLUMN_NAME.toString());

            tableDdl = new TableDdl(new ColumnDefinitionExtractor(hibernateTypeMappings, dialect), entityType);
            formattedTextColDef = tableDdl.getColumnDefinition(richTextProperty + '.' + RichText.FORMATTED_TEXT);
            coreTextColDef = tableDdl.getColumnDefinition(richTextProperty + '.' + RichText.CORE_TEXT);
            stringColumnTypeName = sameProperty
                    ? Placeholder.STRING_COLUMN_TYPE.toString()
                    : tableDdl.getColumnDefinition(stringProperty).sqlTypeName;

            batchDelimiter = switch (dbVersion) {
                case MSSQL -> "GO";
                default -> "";
            };

            // Must be the last statement in the constructor.
            validate();
        }

        private void validate() {
            // Print directly to System.out, as logging may be disabled.

            // New property's column length should not be smaller.
            maybeStringPropertyMetadata.ifPresent(strProp -> {
                if (richTextPropertyMetadata.data().column().length != null &&
                    strProp.data().column().length != null &&
                    richTextPropertyMetadata.data().column().length < strProp.data().column().length)
                {
                    System.out.println(format("[WARN] Length of RichText property [%s] is less than that of String property [%s]. " +
                                              "This may cause issues during data migration.",
                                              richTextPropertyMetadata.name(), strProp.name()));
                }
                else if (richTextPropertyMetadata.data().column().length == null &&
                         strProp.data().column().length != null)
                {
                    System.out.println(format("[WARN] Length of RichText property [%s] is unspecified. " +
                                              "Make sure it is >= length of String property [%s].",
                                              richTextPropertyMetadata.name(), strProp.name()));
                }
            });

            System.out.println();
        }

        @Override
        public String generateSql() {
            final var sb = new StringBuilder();
            generateSqlStatements(s -> {
                sb.append(s);
                if (needsSemicolon(s)) {
                    sb.append(';');
                }
                if (!s.endsWith("\n")) {
                    sb.append('\n');
                }
            });

            System.out.println();
            System.out.println(IMPORTANT_MESSAGE);
            System.out.println();
            return sb.toString();
        }

        @Override
        public void generateSqlStatements(final Consumer<? super String> sqlConsumer) {
            _generateSqlStatements(wrapSqlConsumer(sqlConsumer));
        }

        private void _generateSqlStatements(final Consumer<? super String> sqlConsumer) {
            final var scriptGenDate = dates.now().toDate();

            sqlConsumer.accept(sqlCommentHeader("Generated SQL script for data migration from String to RichText"));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("""
                Entity type: %s
                String property: %s
                RichText property: %s
                Script generation date: %s
                """.formatted(entityType.getTypeName(),
                              maybeStringPropertyMetadata.orElse(richTextPropertyMetadata).name(),
                              richTextPropertyMetadata.name(),
                              scriptGenDate)));
            sqlConsumer.accept("\n");

            sqlConsumer.accept(placeholderDescriptions());
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("If any of the placeholders appear in this script, they should be replaced by suitable values."));

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlCommentHeader("1. Initial migration stage"));
            sqlConsumer.accept("\n");
            generateInitialMigration(sqlConsumer);

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlCommentHeader("2a. Final migration stage"));
            sqlConsumer.accept("\n");
            generateCompleteMigration(sqlConsumer);

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlCommentHeader("2b. Rollback"));
            sqlConsumer.accept("\n");
            generateRollback(scriptGenDate, sqlConsumer);

            System.out.println();
            System.out.println(IMPORTANT_MESSAGE);
            System.out.println();
        }

        private void generateInitialMigration(final Consumer<? super String> sqlConsumer) {
            sqlConsumer.accept(sqlComment("""
            
            ***** IMPORTANT *****
            To ensure correctness of the rollback script, it is necessary to record the date and time when this script (stage 1) is executed.
            
            """));

            // 1. Add a column for each RichText component.
            sqlConsumer.accept(sqlComment("*** Add columns for RichText components."));
            Stream.of(formattedTextColDef.schemaString(dialect, true), coreTextColDef.schemaString(dialect, true))
                    .map(colSchema -> dbVersion.addColumnSql(tableDdl.getTableName(), colSchema))
                    .flatMap(sql -> Stream.of(sql, batchDelimiter))
                    .forEach(sqlConsumer);
            // Create indices for RichText components.
            tableDdl.createNonUniqueIndicesSchema(Stream.of(formattedTextColDef, coreTextColDef), dialect)
                    .stream()
                    .flatMap(sql -> Stream.of(sql, batchDelimiter))
                    .forEach(sqlConsumer);

            // 2. Remove the requiredness constraint of the String property.

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("*** Relax the requiredness constraint of the original String property."));

            // SQL Server requires dependent indices to be dropped first.
            if (dbVersion == DbVersion.MSSQL) {
                sqlConsumer.accept("\n");
                sqlConsumer.accept(sqlComment("""
                    All dependent indices need to be deleted first.
                    The following code dynamically forms and executes a statement that will delete all such indices.
                    """));
                sqlConsumer.accept("\n");
                sqlConsumer.accept(dropAllColumnIndicesMssql(tableDdl.getTableName(), stringColumnName));
                sqlConsumer.accept(batchDelimiter);
                sqlConsumer.accept("\n");
                sqlConsumer.accept(sqlComment("Done deleting dependent indices."));
                sqlConsumer.accept("\n");
            }

            sqlConsumer.accept(dbVersion.alterColumnNullabilitySql(tableDdl.getTableName(), stringColumnName, Placeholder.STRING_COLUMN_TYPE, NULL));
            sqlConsumer.accept(batchDelimiter);

            // 3. Populate values for the RichText property from values for the String property.
            //    a. Formatted text should be populated as HTML, with respect to the client-side RichText editor.
            //       1. The plain text (String property values) should be HTML-encoded (by escaping special characters, such as angle brackets).
            //       2. The encoded text should be enriched with HTML that would be added by the client-side editor.
            //          This step is crucial - without it, the editor may slightly transform the encoded text
            //          (e.g., by wrapping each line in a paragraph element <p>), which would make the property value dirty.
            //    b. Core text should be populated from the plain text as if it had been extracted from the formatted text.

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("*** Populate values for the formatted text component of RichText."));
            sqlConsumer.accept(FormattedTextSql.get(dbVersion).sql(tableDdl.getTableName(), stringColumnName, formattedTextColDef.name));
            sqlConsumer.accept(batchDelimiter);

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("*** Populate values for the core text component of RichText."));
            sqlConsumer.accept(CoreTextSql.get(dbVersion).sql(tableDdl.getTableName(), stringColumnName, coreTextColDef.name));
            sqlConsumer.accept(batchDelimiter);
        }

        private void generateCompleteMigration(final Consumer<? super String> sqlConsumer) {
            // 1. Delete the column for the original String property.
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("*** Delete the column for the original String property."));

            // SQL Server requires dependent indices to be dropped for a column to be deleted.
            if (dbVersion == DbVersion.MSSQL) {
                sqlConsumer.accept("\n");
                sqlConsumer.accept(sqlComment("""
                All dependent indices need to be deleted first.
                The following code dynamically forms and executes a statement that will delete all such indices.
                """));
                sqlConsumer.accept("\n");
                sqlConsumer.accept(dropAllColumnIndicesMssql(tableDdl.getTableName(), stringColumnName));
                sqlConsumer.accept(batchDelimiter);
                sqlConsumer.accept("\n");
                sqlConsumer.accept(sqlComment("Done deleting dependent indices."));
                sqlConsumer.accept("\n");
            }

            // PostgreSQL automatically drops dependent indices and table contraints.
            // https://www.postgresql.org/docs/17/sql-altertable.html#SQL-ALTERTABLE-DESC-DROP-COLUMN

            sqlConsumer.accept(dbVersion.deleteColumnSql(tableDdl.getTableName(), stringColumnName));
            sqlConsumer.accept(batchDelimiter);
            sqlConsumer.accept("\n");

            // 2. If the RichText property has a requiredness constraint, add the {@code NOT NULL} constraint to its columns.
            if (richTextPropertyMetadata.is(REQUIRED)) {
                sqlConsumer.accept(sqlComment("""
                *** Enforce the requiredness constraint for the RichText property.
                Indices, if any exist, need to be dropped beforehand and recreated afterwards.
                Make sure that the following statements are aligned with the current state of indices.
                """));

                sqlConsumer.accept(dbVersion.dropIndexSql(tableDdl.getIndexName(formattedTextColDef), tableDdl.getTableName(), true));
                sqlConsumer.accept(batchDelimiter);
                sqlConsumer.accept(dbVersion.alterColumnNullabilitySql(tableDdl.getTableName(), formattedTextColDef.name, formattedTextColDef.sqlTypeName, NOT_NULL));
                sqlConsumer.accept(batchDelimiter);

                tableDdl.createNonUniqueIndicesSchema(Stream.of(formattedTextColDef), dialect)
                        .stream()
                        .flatMap(sql -> Stream.of(sql, batchDelimiter))
                        .forEach(sqlConsumer);

                sqlConsumer.accept(dbVersion.dropIndexSql(tableDdl.getIndexName(coreTextColDef), tableDdl.getTableName(), true));
                sqlConsumer.accept(batchDelimiter);
                sqlConsumer.accept(dbVersion.alterColumnNullabilitySql(tableDdl.getTableName(), coreTextColDef.name, coreTextColDef.sqlTypeName, NOT_NULL));
                sqlConsumer.accept(batchDelimiter);

                tableDdl.createNonUniqueIndicesSchema(Stream.of(coreTextColDef), dialect)
                        .stream()
                        .flatMap(sql -> Stream.of(sql, batchDelimiter))
                        .forEach(sqlConsumer);
            }
        }

        private void generateRollback(final Date scriptGenDate, final Consumer<? super String> sqlConsumer) {
            // 1. For each new or modified value of the RichText property, populate the String property from the core text component.
            //    This is only possible if the entity type extends AbstractPersistentEntity, which has properties lastUpdatedBy and createdBy.
            sqlConsumer.accept(sqlComment("""
            *** Populate the original String property with new/modified values of RichText property.
            
            ***** IMPORTANT *****
            NOTE: A date is used to identify records that were created after the introduction of the RichText property.
            The value for this date should be set to the moment of executing the stage 1 script.
            Therefore, please adjust the date used below, which, by default, represents the moment when this script was generated.
            """));
            final String sql = switch (dbVersion) {
                case MSSQL, POSTGRESQL -> {
                    final String scriptGenDateSql = dateToSqlString(scriptGenDate);
                    yield """
                      UPDATE %s
                      SET %s = %s
                      WHERE %s > %s OR %s > %s;
                      """.formatted(tableDdl.getTableName(),
                                    stringColumnName,
                                    coreTextColDef.name,
                                    tableDdl.getColumnDefinitionOpt(LAST_UPDATED_DATE)
                                                                      .map(it -> it.name)
                                                                      .orElse(Placeholder.LAST_UPDATED_DATE.toString()),
                                    scriptGenDateSql,
                                    tableDdl.getColumnDefinitionOpt(CREATED_DATE)
                                                                      .map(it -> it.name)
                                                                      .orElse(Placeholder.CREATED_DATE.toString()),
                                    scriptGenDateSql);
                }
                default -> throw new UnsupportedOperationException(dbVersion.toString());
            };
            if (AbstractPersistentEntity.class.isAssignableFrom(entityType)) {
                sqlConsumer.accept(sql);
                sqlConsumer.accept(batchDelimiter);
            }
            else {
                sqlConsumer.accept(sqlComment("""
                The following statement is commented out because it was determined to be non-applicable at the time of script generation.
                Reason: entity type %s does not extend %s, which has the necessary properties [%s].
                """.formatted(entityType.getSimpleName(),
                              AbstractPersistentEntity.class.getSimpleName(),
                              CollectionUtil.toString(List.of(LAST_UPDATED_DATE, AbstractPersistentEntity.CREATED_DATE), ", "))));
                sqlConsumer.accept("\n");
                sqlConsumer.accept(sqlComment(sql));

                sqlConsumer.accept("\n");
                sqlConsumer.accept(sqlComment("Alternatively, the following statement can be used, albeit its much greater scope."));
                final String altSql = switch (dbVersion) {
                    case MSSQL, POSTGRESQL -> "UPDATE %s SET %s = %s".formatted(tableDdl.getTableName(), stringColumnName, coreTextColDef.name);
                    default -> throw new UnsupportedOperationException(dbVersion.toString());
                };
                sqlConsumer.accept(sqlComment(altSql));
                sqlConsumer.accept(sqlComment(batchDelimiter));
            }

            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("Completed population of the original String property."));
            sqlConsumer.accept("\n");

            // 2. If the String property has a requiredness constraint, add the {@code NOT NULL} constraint to its column.
            maybeStringPropertyMetadata.ifPresentOrElse(
                    stringPropertyMetadata -> {
                        if (stringPropertyMetadata.is(REQUIRED)) {
                            sqlConsumer.accept(sqlComment("*** Restore the requiredness constraint for the original String property."));
                            sqlConsumer.accept(dbVersion.alterColumnNullabilitySql(tableDdl.getTableName(), stringColumnName, stringColumnTypeName, NOT_NULL));
                            sqlConsumer.accept(batchDelimiter);
                        }
                    },
                    () -> {
                        // We don't have access to the definition of the String property, so we can't know if it was required or not.
                        sqlConsumer.accept(sqlComment("*** Uncomment to restore the requiredness constraint for the original String property, if it had one."));
                        sqlConsumer.accept(sqlComment(dbVersion.alterColumnNullabilitySql(tableDdl.getTableName(), stringColumnName, stringColumnTypeName, NOT_NULL)));
                        sqlConsumer.accept(sqlComment(batchDelimiter));
                    }
            );

            // 3. Optionally drop columns for the RichText property.
            sqlConsumer.accept(sqlComment("*** If necessary, drop columns for the RichText property."));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("1. Core text"));
            sqlConsumer.accept(sqlComment("Delete all dependent indices first."));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment(dropAllColumnIndicesMssql(tableDdl.getTableName(), coreTextColDef.name)));
            sqlConsumer.accept(sqlComment(batchDelimiter));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("Now drop the column."));
            sqlConsumer.accept(sqlComment(dbVersion.deleteColumnSql(tableDdl.getTableName(), coreTextColDef.name)));
            sqlConsumer.accept(sqlComment(batchDelimiter));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("2. Formatted text"));
            sqlConsumer.accept(sqlComment("Delete all dependent indices first."));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment(dropAllColumnIndicesMssql(tableDdl.getTableName(), formattedTextColDef.name)));
            sqlConsumer.accept(sqlComment(batchDelimiter));
            sqlConsumer.accept("\n");
            sqlConsumer.accept(sqlComment("Now drop the column."));
            sqlConsumer.accept(sqlComment(dbVersion.deleteColumnSql(tableDdl.getTableName(), formattedTextColDef.name)));
            sqlConsumer.accept(sqlComment(batchDelimiter));
        }

    }

    private boolean needsSemicolon(final String s) {
        if (dbVersion == DbVersion.MSSQL) {
            if (s.strip().equals("GO")) {
                return false;
            }
        }

        return !s.isBlank() && !s.startsWith("-- ") && !s.matches("^.*\\s*;\\s*$");
    }

    private Consumer<? super String> wrapSqlConsumer(final Consumer<? super String> sqlConsumer) {
        return sql -> {
            final var theSql = needsSemicolon(sql) ? sql + ';' : sql;
            sqlConsumer.accept(theSql);
        };
    }

    /**
     * Generates statements for SQL Server whose execution will cause all indices that depend on column {@code columnName}
     * in table {@code tableName} to be dropped.
     */
    private static String dropAllColumnIndicesMssql(final CharSequence tableName, final CharSequence columnName) {
        // sys.indexes.type = 0 indicates a Heap, which we ignore.
        // sys.indexes - https://learn.microsoft.com/en-us/sql/relational-databases/system-catalog-views/sys-indexes-transact-sql
        // Heaps - https://learn.microsoft.com/en-us/sql/relational-databases/indexes/heaps-tables-without-clustered-indexes
        // This script is inspired by https://dba.stackexchange.com/questions/2182/dropping-a-constraint-index-on-a-column
        return """
               declare @sql_drop_indices nvarchar(4000);
               select @sql_drop_indices = string_agg(t.s, '; ') from
                   (select ('DROP INDEX ' + quotename(idx.name, '[') + ' ON ' + quotename(tbl.name, '[')) as s
                   from sys.indexes idx inner join
                           sys.tables tbl on idx.object_id = tbl.object_id inner join
                           sys.index_columns idxCol on idx.index_id = idxCol.index_id and idx.object_id = idxCol.object_id inner join
                           sys.columns col on idxCol.column_id = col.column_id and idxCol.object_id = col.object_id
                   where idx.type <> 0
                         and tbl.name = '%s'
                         and col.name = '%s'
                   group by idx.name, tbl.name
                   ) t;

               print @sql_drop_indices;

               EXEC sp_executesql @sql_drop_indices;
               """.formatted(tableName, columnName);
    }

    /**
     * Returns an SQL string literal that represents the specified date.
     */
    private String dateToSqlString(final Date date) {
        try {
            return hibernateDateType.objectToSQLString(date, dialect);
        } catch (final Exception e) {
            throw new IllegalStateException("Error while converting Date [%s] to an SQL string.".formatted(date), e);
        }
    }

    private static String sqlCommentHeader(final CharSequence title) {
        return """
        -- ########################################
        -- # %s
        -- ########################################
        """.formatted(title);
    }

    private static String sqlComment(final CharSequence text) {
        if (StringUtils.contains(text, '\n')) {
            return Pattern.compile(Pattern.quote("\n")).splitAsStream(text)
                    .map(ln -> "-- " + ln)
                    .collect(joining("\n"));
        }
        else {
            return "-- " + text;
        }
    }

    /**
     * Generates an SQL statement that populates a column for the core text component.
     * <p>
     * The algorithm is based on the procedure that extracts core text from formatted text, and it goes thus:
     * <ol>
     *   <li> Replace all whitespace characters by the space character (standard ASCII space, 0x20).
     *   <li> Replace all consecutive space characters by a single space character.
     *   <li> Trim whitespace from both sides.
     * </ol>
     */
    private enum CoreTextSql {
        POSTGRESQL {
            @Override
            public String sql(final String table, final String sourceColumn, final String coreTextColumn) {
                final String expr = "btrim(regexp_replace(%s, '%s+', ' ', 'g'), ' ')".formatted(sourceColumn, PSQL_WHITESPACE_CHAR_PATTERN);
                return "UPDATE %s SET %s = %s".formatted(table, coreTextColumn, expr);
            }
        },

        MSSQL {
            // Arguments to the TRANSLATE function
            // https://learn.microsoft.com/en-us/sql/t-sql/functions/translate-transact-sql
            private static final String CHARACTERS = MSSQL_WHITESPACE_CHARS_STRING;
            private static final String TRANSLATIONS = "'%s'".formatted(" ".repeat(WHITESPACE_CODE_POINTS.length));

            @Override
            public String sql(final String table, final String sourceColumn, final String coreTextColumn) {
                // Replace each whitespace character by a space character
                final String s1 = "translate(%s, %s, %s)".formatted(sourceColumn, CHARACTERS, TRANSLATIONS);
                // Squash consecutive spaces into a single space.
                // This solution is based on https://stackoverflow.com/a/2455869, but instead of using '<>', char(5) and char(6) are used
                // to avoid conflicts with characters in the original string.
                final String s2 = "replace(replace(replace(%s, ' ', char(5) + char(6)), char(6) + char(5), ''), char(5) + char(6), ' ')"
                        .formatted(s1);
                // Trim spaces from both sides
                final String expr = "trim(%s)".formatted(s2);
                return "UPDATE %s SET %s = %s".formatted(table, coreTextColumn, expr);
            }
        };

        /**
         * @param table  name of the table for the entity type
         * @param sourceColumn  name of the column for the original {@code String} property
         * @param coreTextColumn  name of the new column for the core text component
         */
        public abstract String sql(String table, String sourceColumn, String coreTextColumn);

        public static CoreTextSql get(DbVersion dbVersion) {
            return switch (dbVersion) {
                case POSTGRESQL -> POSTGRESQL;
                case MSSQL -> MSSQL;
                default -> throw new UnsupportedOperationException(dbVersion.toString());
            };
        }
    }

    /**
     * Generates an SQL statement that populates a column for the formatted text component.
     * <p>
     * The algorithm is as follows:
     * <ol>
     *   <li> Trim all whitespace from both sides.
     *        (This is not strictly necessary but helps reduce the amount of possible edge cases with respect to the editor.)
     *   <li> Escape special characters:
     *        <ul>
     *          <li> {@code &} -> {@code &amp;}
     *          <li> U+00A0 NO-BREAK SPACE character -> {@code &nbsp;}
     *          <li> {@code <} -> {@code &lt;}
     *          <li> {@code >} -> {@code &gt;}
     *        </ul>
     *        See the end of <a href="https://html.spec.whatwg.org/multipage/parsing.html#serialising-html-fragments">this paragraph of the HTML standard</a>.
     *   <li> Wrap each line in a paragraph element.
     *        Recognised line terminators are: {@code \r\n}, {@code \n}, {@code \r}.
     *   <li> Following the rules of Toast UI, insert <br> into empty paragraphs
     * </ol>
     */
    private enum FormattedTextSql {
        POSTGRESQL {
            /** Regex pattern for PostgreSQL that matches a single whitespace character excluding ({@code \n} and {@code \r}) characters. */
            static final String PSQL_WHITESPACE_NOT_NL_NOT_CR_CHAR_PATTERN = "[%s]".formatted(
                    psqlStringWithUnicodeEscapes(Arrays.stream(WHITESPACE_CODE_POINTS).filter(n -> n != 10 && n != 13).toArray()));

            @Override
            public String sql(final String table, final String sourceColumn, final String formattedTextColumn) {
                // Trim whitespace from both sides
                final String s1 = "btrim(%s, %s)".formatted(sourceColumn, PSQL_WHITESPACE_CHARS_STRING);
                // Escape special characters
                final String s2 = "replace(replace(replace(replace(%s, '&', '&amp;'), %s, '&nbsp;'), '<', '&lt;'), '>', '&gt;')"
                        .formatted(s1, PSQL_NBSP);
                // Wrap each line in a paragraph element. Consecutive empty lines are preserved.
                final String s3 = "regexp_replace(%s, '%s*(\\r\\n|\\n|\\r)%s*', '</p><p>', 'g')"
                        .formatted(s2, PSQL_WHITESPACE_NOT_NL_NOT_CR_CHAR_PATTERN, PSQL_WHITESPACE_NOT_NL_NOT_CR_CHAR_PATTERN);
                final String s4 = "'<p>' || %s || '</p>'".formatted(s3);
                // Following the rules of Toast UI, <br> is inserted into empty paragraphs
                final String expr = "regexp_replace(%s, '<p></p>', '<p><br></p>', 'g')".formatted(s4);
                return "UPDATE %s SET %s = %s".formatted(table, formattedTextColumn, expr);
            }
        },

        MSSQL {
            @Override
            public String sql(final String table, final String sourceColumn, final String formattedTextColumn) {
                // Trim whitespace from both sides
                final String s1 = "trim((%s) from %s)".formatted(MSSQL_WHITESPACE_CHARS_STRING, sourceColumn);
                // Escape special characters
                final String s2 = "replace(replace(replace(replace(%s, '&', '&amp;'), %s, '&nbsp;'), '<', '&lt;'), '>', '&gt;')"
                        .formatted(s1, MSSQL_NBSP);
                // Wrap each line in a paragraph element. Consecutive empty lines are preserved.
                // It is important to first replace '\r\n', and then individual '\n' and '\r' characters, as doing it otherwise is incorrect.
                // Difference from PostgreSQL: leading and trailing whitespace on intermediate lines is not deleted;
                // that would require either regex support or a complex procedure.
                final String s3 = "replace(replace(replace(%s, nchar(13) + nchar(10), '</p><p>'), nchar(10), '</p><p>'), nchar(13), '</p><p>')".formatted(s2);
                final String s4 = "'<p>' + %s + '</p>'".formatted(s3);
                // Following the rules of Toast UI, <br> is inserted into empty paragraphs
                final String expr = "replace(%s, '<p></p>', '<p><br></p>')".formatted(s4);
                return "UPDATE %s SET %s = %s".formatted(table, formattedTextColumn, expr);
            }
        };

        /**
         * @param table  name of the table for the entity type
         * @param sourceColumn  name of the column for the original {@code String} property
         * @param formattedTextColumn  name of the new column for the formatted text component
         */
        public abstract String sql(String table, String sourceColumn, String formattedTextColumn);

        public static FormattedTextSql get(DbVersion dbVersion) {
            return switch (dbVersion) {
                case POSTGRESQL -> POSTGRESQL;
                case MSSQL -> MSSQL;
                default -> throw new UnsupportedOperationException(dbVersion.toString());
            };
        }
    }

    // https://en.wikipedia.org/wiki/Whitespace_character
    private static final int[] WHITESPACE_CODE_POINTS =
            {9, 10, 11, 12, 13, 32, 133, 160, 8192, 8193, 8194, 8195, 8196, 8197, 8198};

    /** A string expression for PostgreSQL composed of all whitespace characters. */
    private static final String PSQL_WHITESPACE_CHARS_STRING = "E'%s'".formatted(psqlStringWithUnicodeEscapes(WHITESPACE_CODE_POINTS));

    /** Regex pattern for PostgreSQL that matches a single whitespace character. */
    private static final String PSQL_WHITESPACE_CHAR_PATTERN = "[%s]".formatted(psqlStringWithUnicodeEscapes(WHITESPACE_CODE_POINTS));

    /** A string expression for PostgreSQL composed of a single non-breaking space character. */
    private static final String PSQL_NBSP = "E'\\u00A0'";

    /** A string expression for SQL Server composed of a single non-breaking space character. */
    private static final String MSSQL_NBSP = "nchar(160)";

    /** A string expression for SQL Server composed of all whitespace characters. */
    private static final String MSSQL_WHITESPACE_CHARS_STRING = IntStream.of(WHITESPACE_CODE_POINTS)
            .mapToObj("nchar(%s)"::formatted)
            .collect(joining("+"));

    /**
     * Returns the contents of a PostgreSQL string, without enclosing quotes, consisting of a given sequence of code points,
     * each of which is represented by a unicode escape character in the resulting string.
     */
    private static String psqlStringWithUnicodeEscapes(final int[] codePoints) {
        return IntStream.of(codePoints)
                .mapToObj("\\u%04x"::formatted)
                .collect(joining(""));
    }

    private enum Placeholder implements CharSequence {

        STRING_COLUMN_NAME ("The column name for the String property being replaced."),
        STRING_COLUMN_TYPE ("The SQL type name (e.g., varchar(100)) of the column for the String property being replaced."),
        LAST_UPDATED_DATE ("The column name for property [%s.%s]".formatted(AbstractPersistentEntity.class.getSimpleName(), AbstractPersistentEntity.LAST_UPDATED_DATE)),
        CREATED_DATE ("The column name for property [%s.%s]".formatted(AbstractPersistentEntity.class.getSimpleName(), AbstractPersistentEntity.CREATED_DATE));

        public final String desc;
        private final String effectiveName;

        Placeholder(final String desc) {
            this.desc = desc;
            this.effectiveName = '<' + name() + '>';
        }

        @Override
        public int length() {
            return effectiveName.length();
        }

        @Override
        public char charAt(final int index) {
            return effectiveName.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return effectiveName.subSequence(start, end);
        }

        @Override
        public String toString() {
            return effectiveName;
        }
    }

    private static String placeholderDescriptions() {
        return Arrays.stream(Placeholder.values())
                .map(placeholder -> "-- %s - %s".formatted(placeholder.name(), placeholder.desc))
                .collect(joining("\n",
                                 sqlCommentHeader("Placeholder descriptions") + "\n",
                                 "\n"));
    }

}
