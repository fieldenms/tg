package ua.com.fielden.platform.ioc.session;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.exceptions.TransactionRollbackDueToThrowable;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.runners.PostgresqlDomainDrivenTestCaseRunner.PostgresqlTestContext;
import ua.com.fielden.platform.test.runners.SqlServerDomainDrivenTestCaseRunner.SqlServerTestContext;
import ua.com.fielden.platform.test_config.ITestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isPostgreSql;
import static ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector.isSqlServer;

/// Tests how [SessionInterceptor] handles errors, in particular, a [StackOverflowError] within a session scope, for example, due to unbounded recursion over cyclic data.
/// Such an error must affect neither later units of work on the same thread, which matters for pooled threads such as those running scheduled jobs,
/// nor units of work on other threads, which obtain connections from the same pool.
///
/// The tests run the real [SessionInterceptor] over a real Hibernate session factory with thread-bound current sessions and HikariCP, following [SessionInterceptorBeginFailureTest].
///
/// Two groups of tests:
///   - Tests that throw an error at a known point run by default.
///   - Tests that overflow the stack run only if system property `stackOverflowTest.enabled` is `true`, as they take minutes, and their outcome depends on where exactly the stack runs out.
///     Each repeats the overflow [#ATTEMPTS] times, every time on a new thread with a small stack ([#STACK_SIZE]), and every time starting from a slightly different stack depth.
///     After each overflow, a unit of work with `@SessionRequired(allowNestedScope = false)` is executed on the same thread, and connections checked out from the pool are counted.
///     Both the number of attempts and the stack size can be set with system properties `stackOverflowTest.attempts` and `stackOverflowTest.stackSizeKb`.
///     Different stack sizes make the stack run out at different points: for example, SQL Server revealed problems with 256 KB, which did not show with 128 KB.
///
public class SessionInterceptorStackOverflowTest {

    private static final boolean STACK_OVERFLOW_TESTS_ENABLED = Boolean.getBoolean("stackOverflowTest.enabled");
    /// A smaller stack overflows after fewer levels of recursion, and thus after fewer database round trips.
    private static final int STACK_SIZE = Integer.getInteger("stackOverflowTest.stackSizeKb", 256) * 1024;
    private static final int ATTEMPTS = Integer.getInteger("stackOverflowTest.attempts", 20);
    /// A stack overflow in the middle of a JDBC exchange may leave the connection waiting for a response that never arrives.
    /// The socket timeout turns such a wait into an error, which is reported as an affected attempt.
    private static final int SOCKET_TIMEOUT_SECONDS = 2;
    private static final long ATTEMPT_TIMEOUT_MILLIS = 30_000;
    /// Fail fast if connections leak and the pool is exhausted, instead of waiting for the default 30 seconds per unit of work.
    private static final long POOL_TIMEOUT_MILLIS = 2_000;

    private SessionFactory sessionFactory;
    private HikariDataSource dataSource;
    private Injector injector;

    @Before
    public void setUp() {
        assumeTrue("The test requires a PostgreSQL or SQL Server test database.", isPostgreSql() || isSqlServer());
        final ITestContext testContext = isPostgreSql() ? new PostgresqlTestContext() : new SqlServerTestContext();
        final Properties dbProps = testContext.mkDbProps(System.getProperty("databaseUri"));

        final var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.URL, withSocketTimeout(dbProps.getProperty("hibernate.connection.url")))
                .applySetting(AvailableSettings.USER, dbProps.getProperty("hibernate.connection.username"))
                .applySetting(AvailableSettings.PASS, dbProps.getProperty("hibernate.connection.password"))
                .applySetting(AvailableSettings.DIALECT, dbProps.getProperty("hibernate.dialect"))
                .applySetting(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread")
                .applySetting("hibernate.hikari.connectionTimeout", String.valueOf(POOL_TIMEOUT_MILLIS))
                .build();
        sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        dataSource = sessionFactory.unwrap(SessionFactoryImplementor.class).getServiceRegistry().getService(ConnectionProvider.class).unwrap(HikariDataSource.class);

        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(subclassesOf(ISessionEnabled.class), annotatedWith(SessionRequired.class), new SessionInterceptor(() -> sessionFactory));
            }
        });

        // Load all classes involved in a unit of work, so that class loading does not happen near the end of the stack.
        injector.getInstance(DatabaseProbe.class).selectOne();
        injector.getInstance(StrictDatabaseProbe.class).selectOne();
    }

    @After
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    /// An error other than [VirtualMachineError] is thrown at a well-defined point, which leaves the connection in sync with the server.
    /// The transaction is rolled back, and the connection is returned to the pool for reuse.
    /// Within a single thread, the pool hands out the connection that this thread returned last.
    ///
    @Test
    public void error_other_than_virtual_machine_error_rolls_back_and_keeps_the_connection() throws SQLException {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final Connection before = probe.physicalConnection();

        assertThrows(TransactionRollbackDueToThrowable.class, () -> probe.selectOneAndThrow(new AssertionError("Purposeful error.")));

        assertFalse(before.isClosed());
        assertSame(before, probe.physicalConnection());
    }

    /// A [VirtualMachineError] may interrupt an exchange with the database, which leaves the connection out of sync with the server.
    /// The connection is aborted instead of being returned to the pool, and the next unit of work on the same thread obtains another connection.
    ///
    @Test
    public void virtual_machine_error_discards_the_connection() throws SQLException {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final Connection before = probe.physicalConnection();

        assertThrows(TransactionRollbackDueToThrowable.class, () -> probe.selectOneAndThrow(new InternalError("Purposeful error.")));

        assertTrue(before.isClosed());
        assertNotSame(before, probe.physicalConnection());
    }

    /// The thread-bound Hibernate session is a JDK dynamic proxy, which wraps an error thrown through it in [UndeclaredThrowableException].
    /// A [VirtualMachineError] wrapped this way is handled the same as an unwrapped one.
    ///
    @Test
    public void exception_caused_by_virtual_machine_error_discards_the_connection() throws SQLException {
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        final Connection before = probe.physicalConnection();

        final var wrapped = new UndeclaredThrowableException(new InvocationTargetException(new InternalError("Purposeful error.")));
        assertSame(wrapped, assertThrows(UndeclaredThrowableException.class, () -> probe.selectOneAndThrow(wrapped)));

        assertTrue(before.isClosed());
        assertNotSame(before, probe.physicalConnection());
    }

    /// Each level of recursion executes a separate unit of work, which completes before the next level.
    /// This is the shape of unbounded recursion over cyclic data, where each step looks up the next entity, for example, walking a management chain that contains a cycle.
    ///
    @Test
    public void stack_overflow_during_recursion_over_separate_units_of_work_does_not_affect_later_units_of_work() throws InterruptedException {
        assumeTrue("Enabled by system property stackOverflowTest.enabled.", STACK_OVERFLOW_TESTS_ENABLED);
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        assertNoUnitOfWorkAffected(new RecursionOverSeparateUnitsOfWork(probe)::descend);
    }

    /// Each level of recursion executes a unit of work nested in the unit of work of the previous level.
    ///
    @Test
    public void stack_overflow_during_recursion_over_nested_units_of_work_does_not_affect_later_units_of_work() throws InterruptedException {
        assumeTrue("Enabled by system property stackOverflowTest.enabled.", STACK_OVERFLOW_TESTS_ENABLED);
        final DatabaseProbe probe = injector.getInstance(DatabaseProbe.class);
        assertNoUnitOfWorkAffected(_ -> probe.descend());
    }

    /// Runs `recursion`, which never terminates normally, [#ATTEMPTS] times, each time on a new thread and after first using a different amount of stack.
    /// Asserts that each recursion overflowed the stack, that a strict unit of work succeeds on the same thread afterwards, and that no connection remains checked out from the pool.
    /// A connection out of sync with the server, if returned to the pool, fails a later unit of work in the same or a later attempt.
    ///
    private void assertNoUnitOfWorkAffected(final Consumer<Integer> recursion) throws InterruptedException {
        final StrictDatabaseProbe strictProbe = injector.getInstance(StrictDatabaseProbe.class);
        final Map<Integer, String> affected = new TreeMap<>();
        int checkedOutConnections = dataSource.getHikariPoolMXBean().getActiveConnections();

        for (int padding = 0; padding < ATTEMPTS; padding++) {
            final int attempt = padding;
            final String[] outcome = new String[3];
            final Thread thread = new Thread(null, () -> {
                try {
                    pad(attempt, () -> recursion.accept(0));
                    outcome[0] = "Recursion terminated without an error.";
                } catch (final Throwable ex) {
                    outcome[0] = isCausedByStackOverflow(ex) ? null : "Unexpected error: " + ex;
                    outcome[2] = causeChain(ex);
                }
                try {
                    strictProbe.selectOne();
                } catch (final Throwable ex) {
                    outcome[1] = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                }
            }, "stack-overflow-" + attempt, STACK_SIZE);
            thread.start();
            thread.join(ATTEMPT_TIMEOUT_MILLIS);
            if (thread.isAlive()) {
                affected.put(attempt, "Hung for more than %s ms.".formatted(ATTEMPT_TIMEOUT_MILLIS));
                continue;
            }

            if (outcome[0] != null) {
                affected.put(attempt, outcome[0]);
            }
            if (outcome[1] != null) {
                affected.merge(attempt, outcome[1], (prev, next) -> prev + " / " + next);
            }
            // The attempt thread has finished, so any connection still checked out from the pool will never be returned.
            final int active = dataSource.getHikariPoolMXBean().getActiveConnections();
            if (active > checkedOutConnections) {
                affected.merge(attempt, "%s connection(s) not returned to the pool".formatted(active - checkedOutConnections), (prev, next) -> prev + " / " + next);
                checkedOutConnections = active;
            }
            System.out.printf("Attempt %s: %s [recursion ended with: %s]%n", attempt, affected.getOrDefault(attempt, "unaffected"), outcome[2]);
        }

        assertTrue("Out of %s attempts, %s were affected (by padding):%n%s".formatted(ATTEMPTS, affected.size(), format(affected)), affected.isEmpty());
    }

    private static String withSocketTimeout(final String url) {
        return isPostgreSql()
               ? url + (url.contains("?") ? "&" : "?") + "socketTimeout=" + SOCKET_TIMEOUT_SECONDS
               : url + ";socketTimeout=" + SOCKET_TIMEOUT_SECONDS * 1000;
    }

    /// Uses `n` additional stack frames before running `action`, which shifts the point where the stack runs out.
    ///
    private static void pad(final int n, final Runnable action) {
        if (n == 0) {
            action.run();
        } else {
            pad(n - 1, action);
        }
    }

    /// The class names of `ex` and its causes, outermost first.
    ///
    private static String causeChain(final Throwable ex) {
        final StringBuilder sb = new StringBuilder();
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            sb.append(sb.isEmpty() ? "" : " <- ").append(cause.getClass().getSimpleName());
        }
        return sb.toString();
    }

    private static boolean isCausedByStackOverflow(final Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof StackOverflowError) {
                return true;
            }
        }
        return false;
    }

    private static String format(final Map<Integer, String> affected) {
        final StringBuilder sb = new StringBuilder();
        affected.forEach((padding, outcome) -> sb.append("  ").append(padding).append(": ").append(outcome).append(System.lineSeparator()));
        return sb.toString();
    }

    /// Recursion, in which each level first executes a unit of work, and then descends to the next level.
    ///
    private record RecursionOverSeparateUnitsOfWork(DatabaseProbe probe) {
        void descend(final int level) {
            probe.selectOne();
            descend(level + 1);
        }
    }

    /// Units of work that query the database within a transaction managed by [SessionInterceptor].
    ///
    public static class DatabaseProbe extends AbstractSessionEnabled {

        @SessionRequired
        public int selectOne() {
            return selectOneInCurrentSession(getSession());
        }

        /// Queries the database and descends into a nested unit of work, indefinitely.
        /// The invocation of `descend()` on `this` is intercepted, as Guice intercepts methods by subclassing.
        ///
        @SessionRequired
        public void descend() {
            selectOneInCurrentSession(getSession());
            descend();
        }

        /// Queries the database, and then throws `error`.
        ///
        @SessionRequired
        public void selectOneAndThrow(final Error error) {
            selectOneInCurrentSession(getSession());
            throw error;
        }

        /// Queries the database, and then throws `exception`.
        ///
        @SessionRequired
        public void selectOneAndThrow(final RuntimeException exception) {
            selectOneInCurrentSession(getSession());
            throw exception;
        }

        /// Returns the physical connection of the current session, as opposed to its proxy from the connection pool.
        ///
        @SessionRequired
        public Connection physicalConnection() {
            return getSession().doReturningWork(connection -> connection.unwrap(Connection.class));
        }
    }

    /// A unit of work that must not be invoked within an existing session scope.
    ///
    public static class StrictDatabaseProbe extends AbstractSessionEnabled {

        @SessionRequired(allowNestedScope = false)
        public int selectOne() {
            return selectOneInCurrentSession(getSession());
        }
    }

    private static int selectOneInCurrentSession(final Session session) {
        return session.doReturningWork(connection -> {
            try (final var statement = connection.createStatement(); final var resultSet = statement.executeQuery("SELECT 1")) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        });
    }

    public abstract static class AbstractSessionEnabled implements ISessionEnabled {
        private Session session;
        private String transactionGuid;

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

}
