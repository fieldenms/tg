package ua.com.fielden.platform.test.db_creators;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.utils.DbUtils;

/**
 * This is a DB creator implementation for running unit tests against H2, running in file mode.
 * 
 * @author TG Team
 *
 */
public class H2DbCreator extends DbCreator {

    public H2DbCreator(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final Properties props, final List<String> maybeDdl)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(testCaseType, props, maybeDdl);
    }

    /**
     * Generates DDL for creation of a test database.
     */
    @Override
    protected List<String> genDdl(final DomainMetadata domainMetaData, final Dialect dialect) {
        final List<String> createDdl = domainMetaData.generateDatabaseDdl(dialect);
        return DbUtils.prependDropDdlForH2(createDdl);
    }

    /**
     * Generate the script for emptying the test database.
     */
    @Override
    protected List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) {
        return entityMetadata.stream().map(entry -> format("TRUNCATE TABLE %s;", entry.getTable())).collect(toList());
    }


    /**
     * Scripts the test database once the test data has been populated, using H2's <code>SCRIPT</code> command.
     */
    @Override
    protected List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException {
        final List<String> insert = new ArrayList<>();

        // create insert statements
        try (final Statement st = conn.createStatement(); final ResultSet rs = st.executeQuery("SCRIPT");) {
            while (rs.next()) {
                final String result = rs.getString(1).trim();
                final String upperCasedResult = result.toUpperCase();
                if (!upperCasedResult.startsWith("INSERT INTO PUBLIC.UNIQUE_ID")
                        && (upperCasedResult.startsWith("INSERT") || upperCasedResult.startsWith("UPDATE") || upperCasedResult.startsWith("DELETE"))) {
                    // resultant script should NOT be UPPERCASED in order not to upperCase for e.g. values,
                    // that was perhaps lover cased while populateDomain() invocation was performed
                    insert.add(result.replace("\n", " ").replace("\r", " "));
                }
            }
        }
        return insert;
    }


}
