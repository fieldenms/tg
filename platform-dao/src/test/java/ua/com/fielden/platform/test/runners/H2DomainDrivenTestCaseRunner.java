package ua.com.fielden.platform.test.runners;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.PlatformDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.db_creators.H2DbCreator;
import ua.com.fielden.platform.test_config.ITestContext;

/**
 * A test case runner for domain-driven unit tests extending {@link AbstractDomainDrivenTestCase} that can use H2 as their database back-end.
 * 
 * @author TG Team
 *
 */
public class H2DomainDrivenTestCaseRunner extends AbstractDomainDrivenTestCaseRunner {

    public H2DomainDrivenTestCaseRunner(final Class<?> klass) throws Exception {
        super(klass, H2DbCreator.class, Optional.empty());
    }
    
    public H2DomainDrivenTestCaseRunner(final Class<?> klass, final IDomainDrivenTestCaseConfiguration config) throws Exception {
        super(klass, H2DbCreator.class, Optional.of(config));
    }

    /**
     * Produces a set of properties for DB connectivity based on the provided <code>dbUri</code>.
     * The URI for H2 looks like <code>./src/test/resources/db/JUNIT_TEST_DB</code>.
     */
    @Override
    protected Properties mkDbProps(final String dbUri) {
        return new H2TestContext(this).mkDbProps(dbUri);
    }

    /**
     * Removes the test db file in addition to the inherited cleanup functionality, which closes db connection.
     */
    @Override
    public void dbCleanUp() {
        new H2TestContext(this).dbCleanUp();
    }
    
    public static class H2TestContext implements ITestContext {
        private final AbstractDomainDrivenTestCaseRunner runner;
        
        public H2TestContext(final AbstractDomainDrivenTestCaseRunner runner) {
            this.runner = runner;
        }
        
        @Override
        public Properties mkDbProps(String dbUri) {
            final Properties props = new Properties();
            props.setProperty("config.domain", PlatformDomainDrivenTestCaseConfiguration.class.getName());
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            // referential integrity is disabled to enable table truncation and test data re-population out of order
            props.setProperty("hibernate.connection.url", format("jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", dbUri));
            props.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            props.setProperty("hibernate.connection.username", "sa");
            props.setProperty("hibernate.connection.password", "");
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
            final Path rootPath = Paths.get(DbCreator.baseDir);
            final String mainDbFileName = runner.databaseUri.substring(runner.databaseUri.lastIndexOf(File.separatorChar) + 1);
            try (final Stream<Path> paths = Files.walk(rootPath)) {
                paths
                    .filter(path -> path.getFileName().toString().contains(mainDbFileName))
                    .map(Path::toFile)
                    .peek(file -> runner.logger.info(format("Removing %s", file.getName())))
                    .forEach(File::delete);
            } catch (final IOException ex) {
                runner.logger.warn("Could not perform proper cleanup.", ex);
            }
        }
        
    }

}
