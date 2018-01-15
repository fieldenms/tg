package ua.com.fielden.platform.test.db_creators;

import java.util.List;
import java.util.Properties;

import org.hibernate.dialect.Dialect;

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
        throw new DomainDriventTestException("Not yet supported.");
    }
}
