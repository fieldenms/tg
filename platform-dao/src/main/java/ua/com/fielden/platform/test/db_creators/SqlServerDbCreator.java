package ua.com.fielden.platform.test.db_creators;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;
import ua.com.fielden.platform.utils.DbUtils;

public class SqlServerDbCreator extends DbCreator {

    public SqlServerDbCreator(final Class<? extends AbstractDomainDrivenTestCase> testCaseType, final String dbName, final List<String> maybeDdl)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(testCaseType, dbName, maybeDdl);
    }

    @Override
    protected List<String> genDdl(final Dialect dialect) {
        final List<String> createDdl = config.getDomainMetadata().generateDatabaseDdl(dialect);
        return DbUtils.prependDropDdlForSqlServer(createDdl);
    }

    @Override
    protected Properties mkDbProps(String dbUri) {
        final Properties dbProps = new Properties();
        // referential integrity is disabled to enable table truncation and test data re-population out of order
        dbProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        dbProps.setProperty("hibernate.connection.url", format("jdbc:sqlserver:%s;queryTimeout=30", dbUri));
        dbProps.setProperty("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dbProps.setProperty("hibernate.connection.username", "SA");
        dbProps.setProperty("hibernate.connection.password", "t32");
        dbProps.setProperty("hibernate.show_sql", "false");
        dbProps.setProperty("hibernate.format_sql", "true");

        return dbProps;
    }

    @Override
    protected List<String> genTruncStmt(Collection<PersistedEntityMetadata<?>> entityMetadata, Connection conn) {
        throw new DomainDriventTestException("Not yet supported");
    }

    @Override
    protected List<String> genInsertStmt(Collection<PersistedEntityMetadata<?>> entityMetadata, Connection conn) throws SQLException {
        throw new DomainDriventTestException("Not yet supported");
    }
}
