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
import ua.com.fielden.platform.test.db_creators.H2DbCreator;

/**
 * A test case runner for domain-driven unit tests extending {@link AbstractDomainDrivenTestCase} that can use H2 as their database back-end.
 * 
 * @author TG Team
 *
 */
public class H2TgDomainDrivenTestCaseRunner extends AbstractDomainDrivenTestCaseRunner {

    public H2TgDomainDrivenTestCaseRunner(final Class<?> klass) throws Exception {
        super(klass, H2DbCreator.class, Optional.empty());
    }
    
    public H2TgDomainDrivenTestCaseRunner(final Class<?> klass, final IDomainDrivenTestCaseConfiguration config) throws Exception {
        super(klass, H2DbCreator.class, Optional.of(config));
    }

    /**
     * Produces a set of properties for DB connectivity based on the provided <code>dbUri</code>.
     * The URI for H2 looks like <code>./src/test/resources/db/JUNIT_TEST_DB</code>.
     */
    @Override
    protected Properties mkDbProps(final String dbUri) {
        final Properties props = new Properties();
        props.setProperty("config.domain", "ua.com.fielden.platform.test.PlatformDomainDrivenTestCaseConfiguration");
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        // referential integrity is disabled to enable table truncation and test data re-population out of order
        props.setProperty("hibernate.connection.url", format("jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", dbUri));
        props.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        props.setProperty("hibernate.connection.username", "sa");
        props.setProperty("hibernate.connection.password", "");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.format_sql", "true");

        return props;
    }

    /**
     * Removes the test db file in addition to the inherited cleanup functionality, which closes db connection.
     */
    @Override
    public void dbCleanUp() {
        super.dbCleanUp();
        
        final Path rootPath = Paths.get(DbCreator.baseDir);
        final String mainDbFileName = databaseUri.substring(databaseUri.lastIndexOf(File.separatorChar) + 1);
        try (final Stream<Path> paths = Files.walk(rootPath)) {
            paths
                .filter(path -> path.getFileName().toString().contains(mainDbFileName))
                .map(Path::toFile)
                .peek(file -> logger.info(format("Removing %s", file.getName())))
                .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
