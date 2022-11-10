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
import ua.com.fielden.platform.utils.DbUtils;

/**
 * This is a DB creator implementation for running unit tests against SQL Server 2012 and up.
 *
 * @author TG Team
 *
 */
public class SqlServerDbCreator extends DbCreator {

    public SqlServerDbCreator(
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
        final List<String> createDdl = DbUtils.prependDropDdlForSqlServer(domainMetaData.generateDatabaseDdl(dialect));
        createDdl.add("EXEC sp_msforeachtable \"ALTER TABLE ? NOCHECK CONSTRAINT all\";");
        createDdl.add(
                "WHILE(EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_TYPE = 'FOREIGN KEY'))"+
                        "BEGIN"+
                        "    DECLARE @sql_alterTable_fk NVARCHAR(4000)"+
                        ""+
                        "    SELECT  TOP 1 @sql_alterTable_fk = ('ALTER TABLE ' + TABLE_SCHEMA + '.[' + TABLE_NAME + '] DROP CONSTRAINT [' + CONSTRAINT_NAME + ']')"+
                        "    FROM    INFORMATION_SCHEMA.TABLE_CONSTRAINTS"+
                        "    WHERE   CONSTRAINT_TYPE = 'FOREIGN KEY'"+
                        ""+
                        "    EXEC (@sql_alterTable_fk)"+
                "END");
        return createDdl;
    }

    /**
     * Generate the script for emptying the test database.
     */
    @Override
    public List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) {
        return entityMetadata.stream().map(entry -> format("TRUNCATE TABLE %s;", entry.getTable())).collect(toList());
    }

    /**
     * Scripts the test database once the test data has been populated, using custom stored procedure <code>sp_generate_inserts</code>.
     * Tables <code>ENTITY_CENTRE_CONFIG</code>, <code>ENTITY_LOCATOR_CONFIG</code> and <code>ENTITY_MASTER_CONFIG</code> are excluded as they contains <code>varbinary</code> columns that cannot be easily scripted.
     */
    @Override
    public List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException {
        // unfortunately we have to drop all the constraints to enable data truncation and repopulation out of order...
        // now let's generate insert statements
        try (final PreparedStatement ps = conn.prepareStatement("EXEC sp_generate_inserts ?")) {
            return entityMetadata.stream()
                .map(PersistedEntityMetadata::getTable)
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
                        logger.warn(format("Could not generate INSERT for table %s", table));
                        logger.warn(ex.getMessage());
                    }
                    return inserts.stream();
                })
                .collect(toList());
        }
    }

    @Override
    public DbVersion dbVersion() {
        return DbVersion.MSSQL;
    }
}

