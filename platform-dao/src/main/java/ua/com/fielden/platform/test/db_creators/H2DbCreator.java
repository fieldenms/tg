package ua.com.fielden.platform.test.db_creators;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.utils.DbUtils;

public class H2DbCreator extends DbCreator {

    public H2DbCreator(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final String dbName, final List<String> maybeDdl)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(testCaseType, dbName, maybeDdl);
    }

    @Override
    protected List<String> genDdl(Dialect dialect) {
        final List<String> createDdl = config.getDomainMetadata().generateDatabaseDdl(dialect);
        return DbUtils.prependDropDdlForH2(createDdl);
    }

    @Override
    protected Properties mkDbProps(final String dbUri) {
        final Properties dbProps = new Properties();
        // referential integrity is disabled to enable table truncation and test data re-population out of order
        dbProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        dbProps.setProperty("hibernate.connection.url", format("jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", dbUri));
        dbProps.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        dbProps.setProperty("hibernate.connection.username", "sa");
        dbProps.setProperty("hibernate.connection.password", "");
        dbProps.setProperty("hibernate.show_sql", "false");
        dbProps.setProperty("hibernate.format_sql", "true");

        return dbProps;
    }
    
    @Override
    protected List<String> genTruncStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) {
        final List<String> trunc = new ArrayList<>();
        // create truncate statements
        for (final PersistedEntityMetadata<?> entry : entityMetadata) {
            trunc.add(format("TRUNCATE TABLE %s;", entry.getTable()));
        }
        
        return trunc;
    }

    @Override
    protected List<String> genInsertStmt(final Collection<PersistedEntityMetadata<?>> entityMetadata, final Connection conn) throws SQLException {
        final List<String> insert = new ArrayList<>();

        // create insert statements
        try (final Statement st = conn.createStatement(); final ResultSet set = st.executeQuery("SCRIPT");) {
            while (set.next()) {
                final String result = set.getString(1).trim();
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
