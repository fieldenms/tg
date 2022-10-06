package ua.com.fielden.platform.test_config;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.PersistedEntityMetadata;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

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
    protected List<String> genDdl(final DomainMetadata domainMetaData, final Dialect dialect) {
        final List<String> result = PostgresqlDbUtils.prependDropDdlForPostgresql(domainMetaData.generateDatabaseDdl(dialect));
        // Drop all the foreign key constraints to allow out-of-order data truncation and/or population.
        // Need to pass the following PL/SQL as a single line otherwise it gets executed one independent line at a time, which does not work.
        result.add("do $$ declare r record; begin for r in (select table_name, constraint_name from information_schema.table_constraints where table_schema = 'public' and constraint_type = 'FOREIGN KEY') loop execute concat('alter table ' || r.table_name || ' drop constraint ' || r.constraint_name); end loop; end $$;");
        return result;
    }

    /**
     * Generate the script for emptying the test database.
     */
    @Override
    public List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) {
        return entityMetadata.stream().map(entry -> format("delete from %s;", entry.getTable())).collect(toList());
    }

    /**
     * Scripts the test database once the test data has been populated, using custom stored procedure <code>create_insert_statement</code>.
     * Tables <code>ENTITY_CENTRE_CONFIG</code>, <code>ENTITY_LOCATOR_CONFIG</code> and <code>ENTITY_MASTER_CONFIG</code> are included even though they contains <code>varbinary</code> columns.
     */
    @Override
    public List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException {
        return entityMetadata.stream()
                .map(PersistedEntityMetadata::getTable)
                .flatMap(table -> {
                    final List<String> inserts = new ArrayList<>();
                    try {
                        // For PostgreSQL the following prepared statement requires the table names as they are, not quoted as strings.
                        // Like unto the difference between:
                        //     SELECT * FROM TABLE_NAME;
                        // and:
                        //     SELECT * FROM 'TABLE_NAME';
                        // The source for this stored procedure can be found in tgpsa-dao/src/main/resources/sql/create_insert_statement.sql
                        try (final PreparedStatement ps = conn.prepareStatement(format("select create_insert_statement(tableoid, %s) from %s", table, table))) {
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