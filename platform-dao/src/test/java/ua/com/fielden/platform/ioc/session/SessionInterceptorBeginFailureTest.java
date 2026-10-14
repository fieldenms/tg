package ua.com.fielden.platform.ioc.session;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.TransactionException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.runners.PostgresqlDomainDrivenTestCaseRunner.PostgresqlTestContext;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner.SqlServerTestContext;
import ua.com.fielden.platform.test_config.ITestContext;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isPostgreSql;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isSqlServer;

/// A database connection may break while [SessionInterceptor] begins a transaction, for example, when the database server resets connections during maintenance.
/// Such a failure must not affect later units of work on the same thread once the database is reachable again.
///
/// The test runs the real [SessionInterceptor] over a real Hibernate session factory with thread-bound current sessions,
/// which is how TG applications are configured (`hibernate.current_session_context_class=thread`).
/// The database is the test database specified by system property `databaseUri`, either PostgreSQL or SQL Server, with connection properties as used by the test runners.
/// Only `SELECT 1` is executed, so the content of the database is irrelevant.
/// The database is accessed through [OutageSimulatingConnectionProvider], which can simulate an outage.
///
public class SessionInterceptorBeginFailureTest {

    private OutageSimulatingConnectionProvider connectionProvider;
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
                .applySetting(AvailableSettings.DIALECT, dbProps.getProperty("hibernate.dialect"))
                .applySetting(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread")
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
    public void session_whose_transaction_failed_to_begin_is_closed() {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);

        connectionProvider.startOutage();
        assertThrows(TransactionException.class, probe::selectOne);

        assertFalse("The session whose transaction failed to begin should be closed, and thus no longer bound to the thread.",
                    probe.getSession().isOpen());
    }

    @Test
    public void unit_of_work_on_the_same_thread_succeeds_once_the_database_is_reachable_again() {
        connectionProvider.startOutage();
        assertThrows(TransactionException.class, injector.getInstance(DatabaseProbe.class)::selectOne);
        connectionProvider.endOutage();

        assertEquals(1, injector.getInstance(DatabaseProbe.class).selectOne());
    }

    /// Units of work on other threads are unaffected by the failure, as each thread has its own current session.
    ///
    @Test
    public void unit_of_work_on_another_thread_succeeds_once_the_database_is_reachable_again() throws InterruptedException, ExecutionException {
        connectionProvider.startOutage();
        assertThrows(TransactionException.class, injector.getInstance(DatabaseProbe.class)::selectOne);
        connectionProvider.endOutage();

        final ExecutorService anotherThread = Executors.newSingleThreadExecutor();
        try {
            assertEquals(Integer.valueOf(1), CompletableFuture.supplyAsync(() -> injector.getInstance(DatabaseProbe.class).selectOne(), anotherThread).get());
        } finally {
            anotherThread.shutdown();
        }
    }

    /// A unit of work that queries the database within a transaction managed by [SessionInterceptor].
    ///
    public static class DatabaseProbe implements ISessionEnabled {
        private Session session;
        private String transactionGuid;

        @SessionRequired
        public int selectOne() {
            return getSession().doReturningWork(connection -> {
                try (final var statement = connection.createStatement(); final var resultSet = statement.executeQuery("SELECT 1")) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            });
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

    /// Provides connections to a database and simulates an outage, which affects connections obtained while the outage lasts.
    ///
    /// Such a connection behaves as a pooled SQL Server connection does when the server resets it:
    ///   - the first round trip to the server fails with `Connection reset by peer` (SQL state `08S01`), as reported by the JDBC driver;
    ///   - any subsequent use fails with `Connection is closed`, as reported by HikariCP once it evicts the broken connection.
    ///
    /// Methods answered from the client-side state of a connection, such as `getAutoCommit()`, involve no round trip and succeed.
    ///
    public static class OutageSimulatingConnectionProvider implements ConnectionProvider {
        private final String url;
        private final String username;
        private final String password;
        private volatile boolean outage = false;

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

        @Override
        public Connection getConnection() throws SQLException {
            final Connection connection = DriverManager.getConnection(url, username, password);
            return outage ? brokenConnection(connection) : connection;
        }

        private static Connection brokenConnection(final Connection connection) {
            final AtomicBoolean reset = new AtomicBoolean(false);
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "close" -> { connection.close(); return null; }
                    case "isClosed" -> { return reset.get(); }
                    case "getAutoCommit" -> { return connection.getAutoCommit(); }
                    case "toString" -> { return "Broken connection"; }
                    case "hashCode" -> { return System.identityHashCode(proxy); }
                    case "equals" -> { return proxy == args[0]; }
                    default -> {
                        if (reset.compareAndSet(false, true)) {
                            throw new SQLException("Connection reset by peer", "08S01");
                        }
                        throw new SQLException("Connection is closed", "08003");
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
