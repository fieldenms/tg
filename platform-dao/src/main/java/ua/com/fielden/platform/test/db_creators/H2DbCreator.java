package ua.com.fielden.platform.test.db_creators;

import java.util.List;

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
    protected List<String> genCreateDbDdl(Dialect dialect) {
        final List<String> createDdl = config.getDomainMetadata().generateDatabaseDdl(dialect);
        return DbUtils.prependDropDdlForH2(createDdl); 
    }

}
