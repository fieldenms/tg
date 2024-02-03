package ua.com.fielden.platform.test.runners;

import static java.lang.String.format;

import java.util.Optional;
import java.util.Properties;

import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.PlatformDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test_config.ITestContext;
import ua.com.fielden.platform.test_config.PostgresqlDbCreator;

/**
 * A test case runner for domain-driven unit tests extending {@link AbstractDomainDrivenTestCase} that can use PostgreSQL as their database back-end.
 *
 * @author TG Team
 */
public class PostgresqlDomainDrivenTestCaseRunner extends AbstractDomainDrivenTestCaseRunner {

    public PostgresqlDomainDrivenTestCaseRunner(final Class<?> klass) throws Exception {
        super(klass, PostgresqlDbCreator.class, Optional.empty());
    }

    public PostgresqlDomainDrivenTestCaseRunner(final Class<?> klass, final IDomainDrivenTestCaseConfiguration config) throws Exception {
        super(klass, PostgresqlDbCreator.class, Optional.of(config));
    }

    /**
     * Produces a set of properties for DB connectivity based on the provided <code>dbUri</code>.
     * The URI for PostgreSQL looks like <code>//localhost:5432/tgpsa_local</code>.
     */
    @Override
    protected Properties mkDbProps(final String dbUri) {
        return new PostgresqlTestContext().mkDbProps(dbUri);
    }

    public static class PostgresqlTestContext implements ITestContext {

        @Override
        public Properties mkDbProps(final String dbUri) {
            final Properties props = new Properties();
            props.setProperty("config.domain", PlatformDomainDrivenTestCaseConfiguration.class.getName());
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.setProperty("hibernate.connection.url", format("jdbc:postgresql:%s", dbUri)); // dbUri should resemble //server_name:5432/database_name
            props.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
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