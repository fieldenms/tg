package ua.com.fielden.platform.ioc.session;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionCommitException;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.runners.PostgresqlDomainDrivenTestCaseRunner.PostgresqlTestContext;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner.SqlServerTestContext;
import ua.com.fielden.platform.test_config.ITestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isPostgreSql;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isSqlServer;

/// A database connection may break, for example, when the database server resets connections during maintenance.
/// Such a failure must not affect later units of work on the same thread, or on other threads, once the database is reachable again.
///
/// Pooled connections have auto-commit disabled, as TG configures them (refer to `HibernateConfigurationFactory`).
/// Beginning a transaction involves no exchange with the database, so a broken connection fails the first statement of a unit of work,
/// which Hibernate reports as [JDBCConnectionException].
///
/// The tests run the real [SessionInterceptor] over a real Hibernate session factory with thread-bound current sessions,
/// which is how TG applications are configured (`hibernate.current_session_context_class=thread`).
/// The database is the test database specified by system property `databaseUri`, either PostgreSQL or SQL Server, with connection properties as used by the test runners.
/// Only `SELECT 1` is executed, so the content of the database is irrelevant.
/// The database is accessed through [OutageSimulatingConnectionProvider], which can simulate an outage.
/// Sessions are discarded by threads that [#discarderThreadFactory] creates, which counts them, and can make starting them fail.
///
public class SessionInterceptorConnectionOutageTest {

    private OutageSimulatingConnectionProvider connectionProvider;
    private final AtomicInteger discarderThreads = new AtomicInteger();
    private volatile boolean failToStartDiscarderThreads = false;
    private final ThreadFactory discarderThreadFactory = runnable -> {
        discarderThreads.incrementAndGet();
        return failToStartDiscarderThreads ? new UnstartableThread(runnable) : Thread.ofPlatform().daemon(true).unstarted(runnable);
    };
    private SessionFactory sessionFactory;
    private Injector injector;

    @Before
    public void setUp() {
        assumeTrue("The test requires a PostgreSQL or SQL Server test database.", isPostgreSql() || isSqlServer());
        final ITestContext testContext = isPostgreSql() ? new PostgresqlTestContext() : new SqlServerTestContext();
        final Properties dbProps = testContext.mkDbProps(System.getProperty("databaseUri"));

        connectionProvider = new OutageSimulatingConnectionProvider(dbProps.getProperty("hibernate.connection.url"),
                                                                    dbProps.getProperty("hibernate.connection.username"),
                                                                    dbProps.getProperty("hibernate.connection.password"));
        final var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.CONNECTION_PROVIDER, connectionProvider)
                .applySetting(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, "true")
                .applySetting(AvailableSettings.DIALECT, dbProps.getProperty("hibernate.dialect"))
                .applySetting(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread")
                .build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();

        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(subclassesOf(ISessionEnabled.class), annotatedWith(SessionRequired.class), new SessionInterceptor(() -> sessionFactory, discarderThreadFactory));
            }
        });
    }

    @After
    public void tearDown() {
        if (sessionFactory == null) {
            return;
        }
        // Release a session that may have been left bound to the test thread.
        final Session leftover = ThreadLocalSessionContext.unbind(sessionFactory);
        if (leftover != null && leftover.isOpen()) {
            leftover.close();
        }
        sessionFactory.close();
    }

    @Test
    public void unit_of_work_succeeds_while_the_database_is_reachable() {
        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    @Test
    public void session_whose_connection_broke_is_closed() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        connectionProvider.startOutage();
        assertThrows(JDBCConnectionException.class, probe::selectOne);

        assertFalse("The session whose connection broke should be closed, and thus no longer bound to the thread.",
                    probe.getSession().isOpen());
    }

    @Test
    public void unit_of_work_on_the_same_thread_succeeds_once_the_database_is_reachable_again() {
        connectionProvider.startOutage();
        assertThrows(JDBCConnectionException.class, injector.getInstance(DatabaseProbe.class)::selectOne);
        connectionProvider.endOutage();

        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    /// Units of work on other threads are unaffected by the failure, as each thread has its own current session.
    ///
    @Test
    public void unit_of_work_on_another_thread_succeeds_once_the_database_is_reachable_again() throws InterruptedException, ExecutionException {
        connectionProvider.startOutage();
        assertThrows(JDBCConnectionException.class, injector.getInstance(DatabaseProbe.class)::selectOne);
        connectionProvider.endOutage();

        final ExecutorService anotherThread = Executors.newSingleThreadExecutor();
        try {
            assertEquals(Integer.valueOf(1), CompletableFuture.supplyAsync(() -> injector.getInstance(DatabaseProbe.class).selectOne(), anotherThread).get());
        } finally {
            anotherThread.shutdown();
        }
    }

    /// Beginning a transaction involves no exchange with the database, so no connection is acquired until the first statement of a unit of work.
    ///
    @Test
    public void beginning_a_transaction_acquires_no_connection() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final int acquiredBefore = connectionProvider.acquiredConnections();

        assertEquals(acquiredBefore, probe.evaluate(connectionProvider::acquiredConnections));
    }

    /// A [VirtualMachineError] may interrupt the first exchange with the database in a unit of work, which leaves the connection out of sync with the server.
    /// The connection is acquired within the unit of work, after its transaction has begun, so the session is discarded:
    /// its connection is aborted instead of being returned to the pool.
    ///
    @Test
    public void virtual_machine_error_during_the_first_exchange_with_the_database_aborts_the_connection() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        final var error = new InternalError("Purposeful error.");
        connectionProvider.failFirstExchangeOfNextConnectionWith(error);
        final Throwable thrown = assertThrows(Throwable.class, probe::selectOne);

        assertTrue("The unit of work should fail with the error, possibly wrapped.", isCausedBy(thrown, error));
        assertEquals(1, connectionProvider.abortedConnections());
        assertFalse(probe.getSession().isOpen());
    }

    /// A connection cannot be acquired for the first statement of a unit of work, for example, because the connection pool is exhausted or the database is unreachable.
    /// The session holds no connection, and thus has no transaction on the server, so it is closed without rolling back, which would attempt to acquire a connection again,
    /// waiting for one a second time, and then discard the session.
    ///
    @Test
    public void failure_to_acquire_a_connection_for_a_statement_closes_the_session_without_acquiring_a_connection_again() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        connectionProvider.makeConnectionsUnavailable(true);
        final int requestedBefore = connectionProvider.requestedConnections();
        assertThrows(JDBCConnectionException.class, probe::selectOne);

        assertEquals(1, connectionProvider.requestedConnections() - requestedBefore);
        assertFalse(probe.getSession().isOpen());
        assertEquals("No session should have been discarded.", 0, discarderThreads.get());

        connectionProvider.makeConnectionsUnavailable(false);
        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    /// A connection cannot be acquired for a statement in a nested scope, and an enclosing scope catches the failure.
    /// The session is closed without rolling back, and the owning invocation completes as it would after a rollback: without committing, and without an error.
    ///
    @Test
    public void failure_to_acquire_a_connection_in_a_nested_scope_caught_by_an_enclosing_scope_completes_the_unit_of_work_as_rolled_back() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        connectionProvider.makeConnectionsUnavailable(true);
        final int requestedBefore = connectionProvider.requestedConnections();
        assertEquals(-1, probe.selectOneInNestedScopeAndCatchFailure());

        assertEquals(1, connectionProvider.requestedConnections() - requestedBefore);
        assertFalse(probe.getSession().isOpen());
        assertEquals("No session should have been discarded.", 0, discarderThreads.get());

        connectionProvider.makeConnectionsUnavailable(false);
        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    /// A unit of work that has executed no statement acquires a connection for committing.
    /// If a connection cannot be acquired, committing fails, and the session is closed without Hibernate rolling back the transaction, which would attempt to acquire a connection again.
    ///
    @Test
    public void failure_to_acquire_a_connection_for_committing_closes_the_session_without_acquiring_a_connection_again() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        connectionProvider.makeConnectionsUnavailable(true);
        final int requestedBefore = connectionProvider.requestedConnections();
        assertThrows(TransactionCommitException.class, () -> probe.evaluate(() -> 1));

        assertEquals(1, connectionProvider.requestedConnections() - requestedBefore);
        assertFalse(probe.getSession().isOpen());
        assertEquals("No session should have been discarded.", 0, discarderThreads.get());

        connectionProvider.makeConnectionsUnavailable(false);
        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    /// Starting a thread may fail, for example, with [OutOfMemoryError] once the process has reached its limit of threads.
    /// The session is then discarded on the current thread instead: its connection is aborted, and the session is closed.
    ///
    @Test
    public void session_is_discarded_on_the_current_thread_if_a_thread_to_discard_it_cannot_be_started() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        failToStartDiscarderThreads = true;
        final var error = new InternalError("Purposeful error.");
        assertSame(error, assertThrows(TransactionRollbackDueToThrowable.class, () -> probe.selectOneAndThrow(error)).getCause());

        assertEquals(1, connectionProvider.abortedConnections());
        assertFalse(probe.getSession().isOpen());
        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    /// Discarding a session fails if a thread to discard it cannot be started, and aborting its connection on the current thread fails too.
    /// The session remains recorded for the thread until discarding it completes, however many invocations on the thread that takes.
    ///
    @Test
    public void session_whose_discarding_fails_remains_recorded_until_a_later_invocation_on_the_same_thread_discards_it() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        failToStartDiscarderThreads = true;
        final var abortFailure = new InternalError("Purposeful abort failure.");
        connectionProvider.failNextAborts(2, abortFailure);

        // The unit of work fails with an error, and discarding its session fails.
        assertSame(abortFailure, assertThrows(InternalError.class, () -> probe.selectOneAndThrow(new InternalError("Purposeful error."))));
        final Session failedSession = probe.getSession();
        assertTrue(failedSession.isOpen());

        // The next invocation fails to discard the session too.
        assertSame(abortFailure, assertThrows(InternalError.class, probe::selectOne));
        assertTrue(failedSession.isOpen());

        // The invocation after that discards the session, and then proceeds.
        assertEquals(1, probe.selectOne());
        assertFalse(failedSession.isOpen());
        assertEquals(1, connectionProvider.abortedConnections());
    }

    private static boolean isCausedBy(final Throwable thrown, final Throwable cause) {
        for (Throwable current = thrown; current != null; current = current.getCause()) {
            if (current == cause) {
                return true;
            }
        }
        return false;
    }

    /// Units of work within a transaction managed by [SessionInterceptor].
    ///
    public static class DatabaseProbe implements ISessionEnabled {
        private Session session;
        private String transactionGuid;

        /// Queries the database.
        ///
        @SessionRequired
        public int selectOne() {
            return selectOneInCurrentSession();
        }

        /// Queries the database, and then throws `error`.
        ///
        @SessionRequired
        public void selectOneAndThrow(final Error error) {
            selectOneInCurrentSession();
            throw error;
        }

        private int selectOneInCurrentSession() {
            return getSession().doReturningWork(connection -> {
                try (final var statement = connection.createStatement(); final var resultSet = statement.executeQuery("SELECT 1")) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            });
        }

        /// Queries the database in a nested scope, and returns `-1` if that fails.
        /// The invocation of `selectOne()` on `this` is intercepted, as Guice intercepts methods by subclassing.
        ///
        @SessionRequired
        public int selectOneInNestedScopeAndCatchFailure() {
            try {
                return selectOne();
            } catch (final RuntimeException _) {
                // The failure is deliberately ignored, as an enclosing scope might do.
                return -1;
            }
        }

        /// Evaluates `supplier` without accessing the database.
        ///
        @SessionRequired
        public int evaluate(final IntSupplier supplier) {
            return supplier.getAsInt();
        }

        @Override
        public Session getSession() {
            return session;
        }

        @Override
        public void setSession(final Session session) {
            this.session = session;
        }

        @Override
        public String getTransactionGuid() {
            return transactionGuid;
        }

        @Override
        public void setTransactionGuid(final String guid) {
            this.transactionGuid = guid;
        }

        @Override
        public User getUser() {
            return null;
        }
    }

    /// A thread that fails to start, as when the process has reached its limit of threads.
    ///
    private static final class UnstartableThread extends Thread {
        UnstartableThread(final Runnable runnable) {
            super(runnable);
        }

        @Override
        public void start() {
            throw new OutOfMemoryError("Purposeful failure: unable to create native thread.");
        }
    }

    /// Provides connections to a database, with auto-commit disabled, as a connection pool configured by TG does.
    /// Simulates an outage, which affects connections obtained while the outage lasts,
    /// and can make the first exchange with the database of the next connection fail with an error.
    /// Can also make connections unavailable, as when the connection pool is exhausted or the database is unreachable,
    /// and make aborting connections fail with an error.
    ///
    /// A connection obtained during an outage behaves as a pooled SQL Server connection does when the server resets it:
    ///   - the first round trip to the server fails with `Connection reset by peer` (SQL state `08S01`), as reported by the JDBC driver;
    ///   - any subsequent use fails with `Connection is closed`, as reported by HikariCP once it evicts the broken connection.
    ///
    /// Methods answered from the client-side state of a connection, such as `getAutoCommit()`, involve no round trip and succeed.
    /// So do `close()` and `abort(Executor)`, which release the connection, unless aborting is made to fail.
    ///
    /// While connections are unavailable, obtaining one fails with [SQLTransientConnectionException], as HikariCP reports a connection timeout.
    ///
    public static class OutageSimulatingConnectionProvider implements ConnectionProvider {
        private final String url;
        private final String username;
        private final String password;
        private volatile boolean outage = false;
        private volatile Error errorForNextConnection;
        private volatile boolean connectionsUnavailable = false;
        private volatile Error abortFailure;
        private final AtomicInteger abortFailuresRemaining = new AtomicInteger();
        private final AtomicInteger requestedConnections = new AtomicInteger();
        private final AtomicInteger acquiredConnections = new AtomicInteger();
        private final AtomicInteger abortedConnections = new AtomicInteger();

        public OutageSimulatingConnectionProvider(final String url, final String username, final String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public void startOutage() {
            outage = true;
        }

        public void endOutage() {
            outage = false;
        }

        /// Makes the first exchange with the database of the next connection fail with `error`, as if the error interrupted it.
        ///
        public void failFirstExchangeOfNextConnectionWith(final Error error) {
            errorForNextConnection = error;
        }

        /// Makes obtaining a connection fail while `unavailable` is `true`.
        ///
        public void makeConnectionsUnavailable(final boolean unavailable) {
            connectionsUnavailable = unavailable;
        }

        /// Makes the next `count` attempts to abort a connection fail with `failure`, leaving the connection open.
        ///
        public void failNextAborts(final int count, final Error failure) {
            abortFailure = failure;
            abortFailuresRemaining.set(count);
        }

        /// The number of attempts to obtain a connection so far, whether they succeeded or not.
        ///
        public int requestedConnections() {
            return requestedConnections.get();
        }

        /// The number of connections provided so far.
        ///
        public int acquiredConnections() {
            return acquiredConnections.get();
        }

        /// The number of connections aborted so far.
        ///
        public int abortedConnections() {
            return abortedConnections.get();
        }

        @Override
        public Connection getConnection() throws SQLException {
            requestedConnections.incrementAndGet();
            if (connectionsUnavailable) {
                throw new SQLTransientConnectionException("Purposeful failure: connection is not available, request timed out.");
            }
            final Connection connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);
            acquiredConnections.incrementAndGet();
            final Error error = errorForNextConnection;
            errorForNextConnection = null;
            return simulatingConnection(connection, outage, error);
        }

        private Connection simulatingConnection(final Connection connection, final boolean broken, final Error firstExchangeError) {
            final AtomicBoolean exchanged = new AtomicBoolean(false);
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "close" -> { connection.close(); return null; }
                    case "abort" -> {
                        if (abortFailuresRemaining.getAndUpdate(n -> Math.max(n - 1, 0)) > 0) {
                            throw abortFailure;
                        }
                        abortedConnections.incrementAndGet();
                        connection.close();
                        return null;
                    }
                    case "isClosed" -> { return broken ? exchanged.get() : connection.isClosed(); }
                    case "getAutoCommit" -> { return connection.getAutoCommit(); }
                    case "toString" -> { return broken ? "Broken connection" : connection.toString(); }
                    case "hashCode" -> { return System.identityHashCode(proxy); }
                    case "equals" -> { return proxy == args[0]; }
                    default -> {
                        final boolean first = exchanged.compareAndSet(false, true);
                        if (broken) {
                            if (first) {
                                throw new SQLException("Connection reset by peer", "08S01");
                            }
                            throw new SQLException("Connection is closed", "08003");
                        }
                        if (first && firstExchangeError != null) {
                            throw firstExchangeError;
                        }
                        try {
                            return method.invoke(connection, args);
                        } catch (final InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    }
                }
            });
        }

        @Override
        public void closeConnection(final Connection connection) throws SQLException {
            connection.close();
        }

        @Override
        public boolean supportsAggressiveRelease() {
            return false;
        }

        @Override
        public boolean isUnwrappableAs(final Class unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(final Class<T> unwrapType) {
            throw new UnsupportedOperationException();
        }
    }

}
