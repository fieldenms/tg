package ua.com.fielden.platform.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.inject.Injector;

import junit.framework.TestCase;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.try_wrapper.TryWrapper;

/**
 * This is base class for any db driven test case. It takes care about Hibernate configuration, session and transaction management as well as test db creation and data population.
 * 
 * The details of Hibernate session creation, injector etc. are provided by a custom implementation of {@link IDbDrivenTestCaseConfiguration}, which is read from
 * src/test/resources/test.properties.
 * 
 * Please note that there can only be one base class for db driven test case per module (e.g. rma-dao, workorder-dao).
 * 
 * @author 01es
 * 
 */
public abstract class DbDrivenTestCase extends TestCase {

    private enum DataSetReason {
        INSERT, UPDATE;
    }

    public static final IDbDrivenTestCaseConfiguration config = createConfig();
    private static String dbDdlFile;
    private static String dbDtdFile;

    @SuppressWarnings("unchecked")
    private static IDbDrivenTestCaseConfiguration createConfig() {
        try {
            final Properties testProps = new Properties();
            final FileInputStream in = new FileInputStream("src/test/resources/test.properties");
            testProps.load(in);
            in.close();

            dbDtdFile = StringUtils.isEmpty(testProps.getProperty("db-dtd-file")) ? "src/test/resources/data-files/db.dtd" : testProps.getProperty("db-dtd-file");
            dbDdlFile = StringUtils.isEmpty(testProps.getProperty("db-ddl-file")) ? "src/test/resources/db/testdb.ddl" : testProps.getProperty("db-ddl-file");

            final String configClassName = testProps.getProperty("config");
            final Class<IDbDrivenTestCaseConfiguration> type = (Class<IDbDrivenTestCaseConfiguration>) Class.forName(configClassName);
            return type.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final EntityFactory entityFactory = config.getEntityFactory();
    public static final Injector injector = config.getInjector();
    public static final HibernateUtil hibernateUtil = config.getHibernateUtil();

    private Transaction transactionForTests;

    static {
        // recreating db schema
        final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        final Transaction tr = hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final IDatabaseConnection connection = getConnection(session).orElseThrow(Result::asRuntime);
        try (final Statement dropObjects = connection.getConnection().createStatement();
             final Statement createIdSequence = connection.getConnection().createStatement();) {
            
            dropObjects.execute("DROP ALL OBJECTS");
            createIdSequence.execute(String.format("CREATE SEQUENCE %s START WITH 10000 INCREMENT BY 1 MINVALUE 1 CACHE  3;", ID_SEQUENCE_NAME));
            
            final List<String> ddls = config.getDdl();
            if (ddls == null || ddls.isEmpty()) {
                connection.getConnection().createStatement().execute("RUNSCRIPT FROM '" + dbDdlFile + "'");
            } else {
                try {
                    for (final String sql : ddls) {
                        try {
                            session.createSQLQuery(sql).executeUpdate();
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (final Exception ex) {
                    tr.rollback();
                    throw new IllegalStateException("Error upon DDL execution: " + ex);
                }
            }
            // The following line of code ensures that database DTD is generated dynamically using the latest schema modifications
            FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream(dbDtdFile));

            tr.commit();
        } catch (final Exception e) {
            System.err.println("Db initialisation failed: " + e.getMessage());
            tr.rollback();
        }
    }

    /**
     * This method should be implemented in descendants in order to provide list of paths to datasets, which are to be used with the given test case (via invoking method
     * getDataSet()). The provided in files data is inserted into the database.
     * 
     * @return
     */
    protected abstract String[] getDataSetPathsForInsert();

    /**
     * Similar to {link #getDataSetPathsForInsert()}, but used to updating existing data. This is useful when an inserted as part of the data from getDataSetPathsForInsert record
     * needs to be updated with a reference to a record that was inserted later also as part of the initial data setup. For example, vehicle may need to have a replacedBy property
     * populated. At the insertion time this is not possible because the relevant replacement vehicle does not yet exist, which leads to referential integrity vilation.
     * <p>
     * By default an empty array of paths if returned.
     * 
     * @return
     */
    protected String[] getDataSetPathsForUpdate() {
        return new String[] {};
    }

    protected static Either<Exception, IDatabaseConnection> getConnection(final Session session) {
        return TryWrapper.Try(() -> {
                    final Method getConnection = session.getClass().getDeclaredMethod("connection");
                    return new DatabaseConnection((Connection) getConnection.invoke(session));
               });
    }

    /**
     * This methods relies on the implementation of the method getDataSetPaths(). It also assumed that names of the flat dataset files should end with .flat.xml in order to be
     * parsed correctly. Non flat dataset files are expected to conform to dataset.dtd.
     * 
     * @return
     * @throws java.lang.Exception
     */
    protected IDataSet getDataSet(final DataSetReason reason) throws Exception {
        IDataSet compositeDataSet = null;
        final String[] dsPaths = reason == DataSetReason.INSERT ? getDataSetPathsForInsert() : getDataSetPathsForUpdate();
        if (dsPaths != null && dsPaths.length > 0) {
            compositeDataSet = new CompositeDataSet(new FlatXmlDataSetBuilder().build(new FileInputStream(dsPaths[0])));
        } else if (reason == DataSetReason.INSERT) {
            final String errorMessage = "Data for test case " + this.getClass().getName()
                    + " has not been provided. Pls override method getDataSetPaths in order to provide the list of paths to required datasets.";
            throw new RuntimeException(errorMessage);
        }
        for (int index = 1; index < dsPaths.length; index++) {
            compositeDataSet = new CompositeDataSet(compositeDataSet, new FlatXmlDataSetBuilder().build(new FileInputStream(dsPaths[index])));
        }

        return compositeDataSet;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        final Transaction localTransaction = hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final IDatabaseConnection connection = getConnection(session).orElseThrow(Result::asRuntime);
        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet(DataSetReason.INSERT));
            final IDataSet updateDataSet = getDataSet(DataSetReason.UPDATE);
            if (updateDataSet != null) {
                DatabaseOperation.UPDATE.execute(connection, updateDataSet);
            }
        } finally {
            localTransaction.commit();
        }
        // each test case to be executed in a separate transaction
        final Session session1 = hibernateUtil.getSessionFactory().getCurrentSession();
        transactionForTests = session1.beginTransaction();
    }

    @Override
    public void tearDown() throws Exception {
        /*
         * commit all changes performed within tests some test cases may require a manual transaction control; thus, the current transaction could be inactive at the tearDown stage
         * -- hence the try/catch
         */
        try {
            transactionForTests.commit();
        } catch (final RuntimeException e) {
        }
        // delete all data from the test db
        final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
        final Transaction localTransaction = hibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        final IDatabaseConnection connection = getConnection(session).orElseThrow(Result::asRuntime);
        DatabaseOperation.DELETE_ALL.execute(connection, getDataSet(DataSetReason.INSERT));
        final IDataSet updateDataSet = getDataSet(DataSetReason.UPDATE);
        if (updateDataSet != null) {
            DatabaseOperation.DELETE_ALL.execute(connection, getDataSet(DataSetReason.UPDATE));
        }

        localTransaction.commit();
        hibernateUtil.getSessionFactory().getCurrentSession().close();
        super.tearDown();
    }
}