package ua.com.fielden.platform.ioc.session;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionImplementor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.SessionInterceptorStackOverflowTest.DatabaseProbe;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionCommitException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.test.runners.PostgresqlDomainDrivenTestCaseRunner.PostgresqlTestContext;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner.SqlServerTestContext;
import ua.com.fielden.platform.test_config.ITestContext;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isPostgreSql;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isSqlServer;

/// Tests how [SessionInterceptor] handles a failure to roll back a transaction after an exception in a unit of work, and a failure to commit a transaction.
/// After a failure that leaves the transaction incomplete, the state of the connection is unknown, so it must not be reused by later units of work.
///
/// The tests run the real [SessionInterceptor] over a real Hibernate session factory with thread-bound current sessions and HikariCP,
/// following [SessionInterceptorStackOverflowTest].
/// Pooled connections have auto-commit disabled, as TG configures them (refer to `HibernateConfigurationFactory`).
/// HikariCP obtains connections from [CompletionFailureSimulatingDataSource], which can make rolling back or committing fail with a SQL state that does not indicate a broken connection.
/// HikariCP reuses a connection after such a failure.
///
public class SessionInterceptorCompletionFailureTest {

    /// Fail fast if connections leak and the pool is exhausted, instead of waiting for the default 30 seconds.
    private static final long POOL_TIMEOUT_MILLIS = 2_000;

    private CompletionFailureSimulatingDataSource completionFailureSimulatingDataSource;
    private HikariDataSource dataSource;
    private SessionFactory sessionFactory;
    private Injector injector;

    @Before
    public void setUp() {
        assumeTrue("The test requires a PostgreSQL or SQL Server test database.", isPostgreSql() || isSqlServer());
        final ITestContext testContext = isPostgreSql() ? new PostgresqlTestContext() : new SqlServerTestContext();
        final Properties dbProps = testContext.mkDbProps(System.getProperty("databaseUri"));

        completionFailureSimulatingDataSource = new CompletionFailureSimulatingDataSource(dbProps.getProperty("hibernate.connection.url"),
                                                                                          dbProps.getProperty("hibernate.connection.username"),
                                                                                          dbProps.getProperty("hibernate.connection.password"));
        final HikariConfig config = new HikariConfig();
        config.setDataSource(completionFailureSimulatingDataSource);
        config.setConnectionTimeout(POOL_TIMEOUT_MILLIS);
        config.setAutoCommit(false);
        dataSource = new HikariDataSource(config);

        final var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DATASOURCE, dataSource)
                .applySetting(AvailableSettings.DIALECT, dbProps.getProperty("hibernate.dialect"))
                .applySetting(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread")
                .applySetting(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, "true")
                .build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();

        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(subclassesOf(ISessionEnabled.class), annotatedWith(SessionRequired.class), new SessionInterceptor(() -> sessionFactory));
            }
        });
    }

    @After
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /// The transaction is rolled back, and the connection is returned to the pool for reuse.
    /// Within a single thread, the pool hands out the connection that this thread returned last.
    ///
    @Test
    public void exception_rolls_back_and_keeps_the_connection() throws SQLException {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final Connection before = probe.physicalConnection();

        final var exception = new IllegalStateException("Purposeful exception.");
        assertSame(exception, assertThrows(IllegalStateException.class, () -> probe.selectOneAndThrow(exception)));

        assertFalse(before.isClosed());
        assertSame(before, probe.physicalConnection());
    }

    /// The session is discarded: its connection is aborted instead of being returned to the pool, and the session is closed, which unbinds it from the thread.
    /// The exception of the unit of work propagates, rather than the failure to roll back.
    ///
    @Test
    public void failure_to_roll_back_discards_the_connection() throws SQLException {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final Connection before = probe.physicalConnection();

        completionFailureSimulatingDataSource.failRollback(true);
        final var exception = new IllegalStateException("Purposeful exception.");
        try {
            assertSame(exception, assertThrows(IllegalStateException.class, () -> probe.selectOneAndThrow(exception)));
        } finally {
            completionFailureSimulatingDataSource.failRollback(false);
        }

        assertFalse(probe.getSession().isOpen());
        assertTrue(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertNotSame(before, probe.physicalConnection());
    }

    /// The JDBC commit fails, which leaves its outcome unknown, and Hibernate does not roll back a transaction whose commit failed.
    /// The session is discarded, and as it still holds its connection, the connection is aborted instead of being returned to the pool.
    ///
    @Test
    public void failure_of_jdbc_commit_discards_the_connection() throws SQLException {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final Connection before = probe.physicalConnection();

        completionFailureSimulatingDataSource.failCommit(true);
        try {
            assertThrows(TransactionCommitException.class, probe::selectOne);
        } finally {
            completionFailureSimulatingDataSource.failCommit(false);
        }

        assertFalse(probe.getSession().isOpen());
        assertTrue(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertNotSame(before, probe.physicalConnection());
    }

    /// Committing fails before the JDBC commit, as flushing might, for example, when violating a constraint.
    /// Hibernate rolls back the transaction and the session closes itself, which returns the connection to the pool for reuse.
    /// Discarding skips the closed session, so such ordinary failures cost no connection.
    ///
    @Test
    public void failure_before_jdbc_commit_rolls_back_and_keeps_the_connection() throws SQLException {
        final CommitFailureProbe probe = injector.getInstance(CommitFailureProbe.class);
        final Connection before = probe.physicalConnection();

        assertThrows(TransactionCommitException.class, () -> probe.selectOneAndFailBeforeCommit(new IllegalStateException("Purposeful exception.")));

        assertFalse(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertSame(before, probe.physicalConnection());
    }

    /// Committing fails before the JDBC commit, and Hibernate then fails to roll back the transaction.
    /// The session is discarded, and as it still holds its connection, the connection is aborted, as after any failure to roll back.
    ///
    @Test
    public void failure_before_jdbc_commit_followed_by_failure_to_roll_back_discards_the_connection() throws SQLException {
        final CommitFailureProbe probe = injector.getInstance(CommitFailureProbe.class);
        final Connection before = probe.physicalConnection();

        completionFailureSimulatingDataSource.failRollback(true);
        try {
            assertThrows(TransactionCommitException.class, () -> probe.selectOneAndFailBeforeCommit(new IllegalStateException("Purposeful exception.")));
        } finally {
            completionFailureSimulatingDataSource.failRollback(false);
        }

        assertFalse(probe.getSession().isOpen());
        assertTrue(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertNotSame(before, probe.physicalConnection());
    }

    /// Committing fails before the JDBC commit with an error other than a [VirtualMachineError], as flushing might, for example, with [AssertionError].
    /// Hibernate neither commits nor rolls back after an error, so the transaction is rolled back, as after such an error in the unit of work itself,
    /// and the connection is returned to the pool for reuse, so that an error recurring at commit does not cost a connection each time.
    ///
    @Test
    public void error_other_than_virtual_machine_error_before_jdbc_commit_rolls_back_and_keeps_the_connection() throws SQLException {
        final CommitFailureProbe probe = injector.getInstance(CommitFailureProbe.class);
        final Connection before = probe.physicalConnection();

        final var error = new AssertionError("Purposeful error.");
        assertSame(error, assertThrows(TransactionRollbackDueToThrowable.class, () -> probe.selectOneAndFailBeforeCommit(error)).getCause());

        assertFalse(probe.getSession().isOpen());
        assertFalse(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertSame(before, probe.physicalConnection());
    }

    /// As [#error_other_than_virtual_machine_error_before_jdbc_commit_rolls_back_and_keeps_the_connection], but rolling back fails too.
    /// The session is discarded, which aborts its connection, as after any failure to roll back.
    ///
    @Test
    public void error_other_than_virtual_machine_error_before_jdbc_commit_followed_by_failure_to_roll_back_discards_the_connection() throws SQLException {
        final CommitFailureProbe probe = injector.getInstance(CommitFailureProbe.class);
        final Connection before = probe.physicalConnection();

        completionFailureSimulatingDataSource.failRollback(true);
        final var error = new AssertionError("Purposeful error.");
        try {
            assertSame(error, assertThrows(TransactionRollbackDueToThrowable.class, () -> probe.selectOneAndFailBeforeCommit(error)).getCause());
        } finally {
            completionFailureSimulatingDataSource.failRollback(false);
        }

        assertFalse(probe.getSession().isOpen());
        assertTrue(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertNotSame(before, probe.physicalConnection());
    }

    /// A unit of work whose transaction has been marked for rollback only, for example, after a failed statement, invokes a nested scope, which fails,
    /// and rolling back then fails too.
    /// The session is discarded, which aborts its connection, as the nested scope uses the session underlying the thread-bound proxy, which the owning invocation obtained.
    /// The proxy rejects unwrapping it, unless the transaction is active, and would not let the connection be aborted.
    ///
    @Test
    public void failure_to_roll_back_after_a_nested_scope_fails_within_a_transaction_marked_for_rollback_only_discards_the_connection() throws SQLException {
        final MarkedRollbackProbe probe = injector.getInstance(MarkedRollbackProbe.class);
        final Connection before = probe.physicalConnection();

        completionFailureSimulatingDataSource.failRollback(true);
        final var exception = new IllegalStateException("Purposeful exception.");
        try {
            assertSame(exception, assertThrows(IllegalStateException.class, () -> probe.selectOneMarkRollbackOnlyAndThrowInNestedScope(exception)));
        } finally {
            completionFailureSimulatingDataSource.failRollback(false);
        }

        assertFalse(probe.getSession().isOpen());
        assertTrue(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertNotSame(before, probe.physicalConnection());
    }

    /// Units of work whose transaction is marked for rollback only before they invoke a nested scope, as a failed statement would mark it.
    ///
    public static class MarkedRollbackProbe extends DatabaseProbe {

        @SessionRequired
        public void selectOneMarkRollbackOnlyAndThrowInNestedScope(final RuntimeException exception) {
            selectOne();
            getSession().getTransaction().markRollbackOnly();
            throwInNestedScope(exception);
        }

        /// The invocation of this method on `this` is intercepted, as Guice intercepts methods by subclassing.
        ///
        @SessionRequired
        public void throwInNestedScope(final RuntimeException exception) {
            throw exception;
        }
    }

    /// Units of work whose commit fails before the JDBC commit.
    ///
    public static class CommitFailureProbe extends DatabaseProbe {

        /// Queries the database, and then returns.
        /// Committing fails, as a process that Hibernate runs before completing the transaction throws `exception`.
        ///
        @SessionRequired
        public int selectOneAndFailBeforeCommit(final RuntimeException exception) {
            final BeforeTransactionCompletionProcess failingProcess = _ -> { throw exception; };
            getSession().unwrap(SessionImplementor.class).getActionQueue().registerProcess(failingProcess);
            return selectOne();
        }

        /// Queries the database, and then returns.
        /// Committing fails, as a process that Hibernate runs before completing the transaction throws `error`, which Hibernate propagates unchanged.
        ///
        @SessionRequired
        public int selectOneAndFailBeforeCommit(final Error error) {
            final BeforeTransactionCompletionProcess failingProcess = _ -> { throw error; };
            getSession().unwrap(SessionImplementor.class).getActionQueue().registerProcess(failingProcess);
            return selectOne();
        }
    }

    /// Provides connections to a database, whose `rollback()` or `commit()` fails while enabled by [#failRollback(boolean)] or [#failCommit(boolean)].
    ///
    /// The failure has SQL state `HY000` (general error), which does not indicate a broken connection.
    /// All other methods are delegated to the connection obtained from the driver.
    ///
    public static class CompletionFailureSimulatingDataSource implements DataSource {
        private final String url;
        private final String username;
        private final String password;
        private volatile boolean failRollback = false;
        private volatile boolean failCommit = false;

        public CompletionFailureSimulatingDataSource(final String url, final String username, final String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public void failRollback(final boolean failRollback) {
            this.failRollback = failRollback;
        }

        public void failCommit(final boolean failCommit) {
            this.failCommit = failCommit;
        }

        @Override
        public Connection getConnection() throws SQLException {
            final Connection connection = DriverManager.getConnection(url, username, password);
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "rollback" -> {
                        if (failRollback && args == null) {
                            throw new SQLException("Purposeful rollback failure.", "HY000");
                        }
                    }
                    case "commit" -> {
                        if (failCommit) {
                            throw new SQLException("Purposeful commit failure.", "HY000");
                        }
                    }
                    case "hashCode" -> { return System.identityHashCode(proxy); }
                    case "equals" -> { return proxy == args[0]; }
                    default -> {}
                }
                try {
                    return method.invoke(connection, args);
                } catch (final InvocationTargetException ex) {
                    throw ex.getCause();
                }
            });
        }

        @Override
        public Connection getConnection(final String username, final String password) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(final PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(final int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            throw new SQLException("Not a wrapper.");
        }

        @Override
        public boolean isWrapperFor(final Class<?> iface) {
            return false;
        }
    }

}
