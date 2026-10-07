package ua.com.fielden.platform.ioc.session;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.ioc.session.SessionInterceptorStackOverflowTest.DatabaseProbe;
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

/// Tests how [SessionInterceptor] handles a failure to roll back a transaction after an exception in a unit of work.
/// After such a failure, the state of the connection is unknown, so it must not be reused by later units of work.
///
/// The tests run the real [SessionInterceptor] over a real Hibernate session factory with thread-bound current sessions and HikariCP,
/// following [SessionInterceptorStackOverflowTest].
/// HikariCP obtains connections from [RollbackFailureSimulatingDataSource], which can make rolling back fail with a SQL state that does not indicate a broken connection.
/// HikariCP reuses a connection after such a failure.
///
public class SessionInterceptorRollbackFailureTest {

    /// Fail fast if connections leak and the pool is exhausted, instead of waiting for the default 30 seconds.
    private static final long POOL_TIMEOUT_MILLIS = 2_000;

    private RollbackFailureSimulatingDataSource rollbackFailureSimulatingDataSource;
    private HikariDataSource dataSource;
    private SessionFactory sessionFactory;
    private Injector injector;

    @Before
    public void setUp() {
        assumeTrue("The test requires a PostgreSQL or SQL Server test database.", isPostgreSql() || isSqlServer());
        final ITestContext testContext = isPostgreSql() ? new PostgresqlTestContext() : new SqlServerTestContext();
        final Properties dbProps = testContext.mkDbProps(System.getProperty("databaseUri"));

        rollbackFailureSimulatingDataSource = new RollbackFailureSimulatingDataSource(dbProps.getProperty("hibernate.connection.url"),
                                                                                      dbProps.getProperty("hibernate.connection.username"),
                                                                                      dbProps.getProperty("hibernate.connection.password"));
        final HikariConfig config = new HikariConfig();
        config.setDataSource(rollbackFailureSimulatingDataSource);
        config.setConnectionTimeout(POOL_TIMEOUT_MILLIS);
        dataSource = new HikariDataSource(config);

        final var registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DATASOURCE, dataSource)
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

        rollbackFailureSimulatingDataSource.failRollback(true);
        final var exception = new IllegalStateException("Purposeful exception.");
        try {
            assertSame(exception, assertThrows(IllegalStateException.class, () -> probe.selectOneAndThrow(exception)));
        } finally {
            rollbackFailureSimulatingDataSource.failRollback(false);
        }

        assertFalse(probe.getSession().isOpen());
        assertTrue(before.isClosed());
        assertEquals(0, dataSource.getHikariPoolMXBean().getActiveConnections());
        assertNotSame(before, probe.physicalConnection());
    }

    /// Provides connections to a database, whose `rollback()` fails while enabled by [#failRollback(boolean)].
    ///
    /// The failure has SQL state `HY000` (general error), which does not indicate a broken connection.
    /// All other methods are delegated to the connection obtained from the driver.
    ///
    public static class RollbackFailureSimulatingDataSource implements DataSource {
        private final String url;
        private final String username;
        private final String password;
        private volatile boolean failRollback = false;

        public RollbackFailureSimulatingDataSource(final String url, final String username, final String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public void failRollback(final boolean failRollback) {
            this.failRollback = failRollback;
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
