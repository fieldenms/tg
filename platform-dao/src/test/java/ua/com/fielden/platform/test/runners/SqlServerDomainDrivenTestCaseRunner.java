package ua.com.fielden.platform.test.runners;

import static java.lang.String.format;

import java.util.Optional;
import java.util.Properties;

import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.PlatformDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test_config.ITestContext;
import ua.com.fielden.platform.test_config.SqlServerDbCreator;

/// A test case runner for domain-driven unit tests extending [AbstractDomainDrivenTestCase] that can use SQL Server 2012 and up as their database back-end.
///
public class SqlServerDomainDrivenTestCaseRunner extends AbstractDomainDrivenTestCaseRunner {

    public SqlServerDomainDrivenTestCaseRunner(final Class<?> klass) throws Exception {
        super(klass, SqlServerDbCreator.class, Optional.empty());
    }

    public SqlServerDomainDrivenTestCaseRunner(final Class<?> klass, final IDomainDrivenTestCaseConfiguration config) throws Exception {
        super(klass, SqlServerDbCreator.class, Optional.of(config));
    }

    /// Produces a set of properties for DB connectivity based on the provided `dbUri`.
    /// The URI for SQL Server looks like `//192.168.1.142:1433;database=JUNIT_TEST_DB_1`.
    ///
    @Override
    protected Properties mkDbProps(final String dbUri) {
        return new SqlServerTestContext().mkDbProps(dbUri);
    }

    public static class SqlServerTestContext implements ITestContext {
        
        @Override
        public Properties mkDbProps(String dbUri) {
            final Properties props = new Properties();
            props.setProperty("config.domain", PlatformDomainDrivenTestCaseConfiguration.class.getName());
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServer2012Dialect");
            props.setProperty("hibernate.connection.url", format(dbUri.contains("queryTimeout") ? "jdbc:sqlserver:%s" : "jdbc:sqlserver:%s;queryTimeout=30", dbUri));
            props.setProperty("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
            props.setProperty("hibernate.connection.username", "junit");
            props.setProperty("hibernate.connection.password", "junit");
            props.setProperty("hibernate.show_sql", "false");
            props.setProperty("hibernate.format_sql", "true");
            props.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            props.setProperty("hibernate.hikari.connectionTimeout", "5000"); // 5 seconds, maximum waiting time for a connection from the pool
            props.setProperty("hibernate.hikari.minimumIdle", "2"); // minimum number of ideal connections in the pool
            props.setProperty("hibernate.hikari.maximumPoolSize", "8"); // maximum number of actual connection in the pool
            props.setProperty("hibernate.hikari.idleTimeout", "240000"); // 4 minutes, maximum time that a connection is allowed to sit idle in the pool            
            return props;
        }

        @Override
        public void dbCleanUp() {
        }
        
    }

}
