package ua.com.fielden.platform.test_config;

import com.google.common.collect.ImmutableList;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.ddl.IDdlGenerator;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.utils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * This is a DB creator implementation for running unit tests against PostgreSQL.
 *
 * @author TG Team
 */
public class PostgresqlDbCreator extends DbCreator {

    public PostgresqlDbCreator(
            final Class<? extends AbstractDomainDrivenTestCase> testCaseType,
            final Properties props,
            final IDomainDrivenTestCaseConfiguration config,
            final List<String> maybeDdl,
            final boolean execDdslScripts)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(testCaseType, props, config, maybeDdl, execDdslScripts);
    }

    /**
     * Generates DDL for creation of a test database. All constraints are dropped to enable out-of-order data insertion and table truncation.
     */
    @Override
    protected List<String> genDdl(final IDdlGenerator ddlGenerator, final Dialect dialect) {
        final var ddl = DbUtils.prependDropDdlForPostgresql(ddlGenerator.generateDatabaseDdl(dialect, false));
        return ImmutableList.<String>builder()
                .add("BEGIN TRANSACTION")
                .addAll(ddl)
                .add("COMMIT")
                .build();
    }

    /**
     * Generate the script for emptying the test database.
     */
    @Override
    public List<String> genTruncStmt(final Collection<EntityMetadata.Persistent> entityMetadata, final Connection conn) {
        return entityMetadata.stream().map(em -> format("delete from %s;", em.data().tableName())).collect(toList());
    }

    /**
     * Scripts the test database once the test data has been populated, using custom stored procedure <code>create_insert_statement</code>.
     * Tables <code>ENTITY_CENTRE_CONFIG</code>, <code>ENTITY_LOCATOR_CONFIG</code> and <code>ENTITY_MASTER_CONFIG</code> are included even though they contains <code>varbinary</code> columns.
     */
    @Override
    public List<String> genInsertStmt(final Collection<EntityMetadata.Persistent> entityMetadata, final Connection conn) {
        return entityMetadata.stream()
                .map(em -> em.data().tableName())
                .flatMap(table -> {
                    final List<String> inserts = new ArrayList<>();
                    try {
                        // For PostgreSQL the following prepared statement requires the table names as they are, not quoted as strings.
                        // Like unto the difference between:
                        //     SELECT * FROM TABLE_NAME;
                        // and:
                        //     SELECT * FROM 'TABLE_NAME';
                        // The source for this stored procedure can be found in tgpsa-dao/src/main/resources/sql/create_insert_statement.sql

                        // Must pass mytable.* to create_insert_statement: if table "mytable" has column named "mytable",
                        // then "mytable" will refer to the column, not the table.
                        try (final PreparedStatement ps = conn.prepareStatement("select create_insert_statement(tableoid, %1$s.*) from %1$s".formatted(table))) {
                            try (final ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    inserts.add(rs.getString(1));
                                }
                            }
                        }
                    } catch (final Exception ex) {
                        logger.warn(format("Could not generate INSERT for table %s", table));
                        logger.warn(ex.getMessage());
                    }
                    return inserts.stream();
                })
                .collect(toList());
    }

    @Override
    public DbVersion dbVersion() {
        return DbVersion.POSTGRESQL;
    }

}
