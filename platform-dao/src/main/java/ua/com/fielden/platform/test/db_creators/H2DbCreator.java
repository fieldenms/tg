package ua.com.fielden.platform.test.db_creators;

import static java.lang.String.format;

import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

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
        // TODO Due to incorrect generation of constraints by Hibernate, at this stage simply disable REFERENTIAL_INTEGRITY by rewriting URL
        //      This should be modified once correct db schema generation is implemented
        dbProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        dbProps.setProperty("hibernate.connection.url", format("jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE", dbUri));
        dbProps.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        dbProps.setProperty("hibernate.connection.username", "sa");
        dbProps.setProperty("hibernate.connection.password", "");
        dbProps.setProperty("hibernate.show_sql", "false");
        dbProps.setProperty("hibernate.format_sql", "true");

        return dbProps;
    }

}
