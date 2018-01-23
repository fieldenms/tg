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
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.utils.DbUtils;

/**
 * This is a DB creator implementation for running unit tests against H2, running in file mode.
 * 
 * @author TG Team
 *
 */
public class H2DbCreator extends DbCreator {

    public H2DbCreator(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final Properties props, final IDomainDrivenTestCaseConfiguration config, final List<String> maybeDdl)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(testCaseType, props, config, maybeDdl);
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
    public List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) {
        return entityMetadata.stream().map(entry -> format("TRUNCATE TABLE %s;", entry.getTable())).collect(toList());
    }


    /**
     * Scripts the test database once the test data has been populated, using H2's <code>SCRIPT</code> command.
     */
    @Override
    public List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException {
        final List<String> inserts = new ArrayList<>();
        // create insert statements
        try (final Statement st = conn.createStatement(); final ResultSet rs = st.executeQuery("SCRIPT");) {
            while (rs.next()) {
                final String result = rs.getString(1).trim();
                final String upperCasedResult = result.toUpperCase();
                // the SCRIPT command returns all the scripts to recreated the database
                // we're interested only in INSERT statements
                if (upperCasedResult.startsWith("INSERT")) {
                    inserts.addAll(transformToIndividualInsertStmts(result));
                    //inserts.add(result);
                }
            }
        }
        return inserts;
    }

    /**
     * Transforms an insert statement for a single table, which H2 produces the VALUES part that takes a list of tuples for multiple rows, into a complete series of insert statements.
     * This is necessary to better control insertion of individual records. 
     * 
     * @param origInsertStmt
     * @return
     */
    private static List<String> transformToIndividualInsertStmts(final String origInsertStmt) {
        // here is the expected structure of the passed in string as observed during H2 scripting analysis
        //            INSERT ... VALUES\n
        //            (...),\n
        //            (...);
        final String[] lines = origInsertStmt.split("\n");
        final String insertAndValuesPart = lines[0].replace("\r", " "); // let's remove \r as a token of love for Windows users 
        
        final List<String> allInserts = new ArrayList<>();
        for (int index = 1; index < lines.length; index++) {
            final StringBuilder stmt = new StringBuilder();
            stmt.append(insertAndValuesPart);
            final String tupleStmt = lines[index].replace("\r", ""); // let's remove \r as a token of love for Windows users
            if (tupleStmt.endsWith(",")) {
                stmt.append(tupleStmt.substring(0, tupleStmt.length() - 1));
                stmt.append(";");
            } else {
                stmt.append(tupleStmt);
            }
            
            allInserts.add(stmt.toString());
        }
        
        return allInserts;
    }
}
