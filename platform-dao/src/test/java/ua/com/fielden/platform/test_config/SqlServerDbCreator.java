package ua.com.fielden.platform.test_config;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.ddl.IDdlGenerator;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;
import ua.com.fielden.platform.utils.DbUtils;

/// This is a DB creator implementation for running unit tests against SQL Server 2012 and up.
///
public class SqlServerDbCreator extends DbCreator {

    public SqlServerDbCreator(
            final Class<? extends AbstractDomainDrivenTestCase> testCaseType,
            final Properties props,
            final IDomainDrivenTestCaseConfiguration config,
            final List<String> maybeDdl,
            final boolean execDdlScripts)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        super(testCaseType, props, config, maybeDdl, execDdlScripts);
    }

    /**
     * Generates DDL for creation of a test database. All constraints are dropped to enable out-of-order data insertion and table truncation.
     */
    @Override
    protected List<String> genDdl(final IDdlGenerator ddlGenerator, final Dialect dialect) {
        return DbUtils.prependDropDdlForSqlServer(ddlGenerator.generateDatabaseDdl(dialect, false));
    }

    /**
     * Generate the script for emptying the test database.
     */
    @Override
    public List<String> genTruncStmt(final Collection<EntityMetadata.Persistent> entityMetadata, final Connection conn) {
        return entityMetadata.stream().map(em -> format("TRUNCATE TABLE %s;", em.data().tableName())).collect(toList());
    }

    /**
     * Scripts the test database once the test data has been populated, using custom stored procedure <code>sp_generate_inserts</code>.
     * Tables <code>ENTITY_CENTRE_CONFIG</code>, <code>ENTITY_LOCATOR_CONFIG</code> and <code>ENTITY_MASTER_CONFIG</code> are excluded as they contains <code>varbinary</code> columns that cannot be easily scripted.
     */
    @Override
    public List<String> genInsertStmt(final Collection<EntityMetadata.Persistent> entityMetadata, final Connection conn) {
        // unfortunately we have to drop all the constraints to enable data truncation and repopulation out of order...
        // now let's generate insert statements
        try (final PreparedStatement ps = conn.prepareStatement("EXEC sp_generate_inserts ?")) {
            return entityMetadata.stream()
                .map(em -> em.data().tableName())
                .filter(table -> !"ENTITY_CENTRE_CONFIG".equals(table) && !"ENTITY_LOCATOR_CONFIG".equals(table) && !"ENTITY_MASTER_CONFIG".equals(table))
                .flatMap(table -> {
                    final List<String> inserts = new ArrayList<>();
                    try {
                        ps.setString(1, table);
                        try (final ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                inserts.add(rs.getString(1));
                            }
                        }
                    } catch (final Exception ex) {
                        logger.warn(() -> format("Could not generate INSERT for table %s", table));
                        logger.warn(ex.getMessage());
                    }
                    return inserts.stream();
                })
                .collect(toList());
        } catch (final Exception ex) {
            throw new DomainDrivenTestException("Could not generate insert statements.", ex);
        }
    }

    @Override
    public DbVersion dbVersion() {
        return DbVersion.MSSQL;
    }
}

