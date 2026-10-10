package ua.com.fielden.platform.ioc;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.hikaricp.internal.HikariConfigurationUtil;
import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.ioc.HibernateConfigurationFactory.ERR_AUTOCOMMIT_SETTING_SPECIFIED;
import static ua.com.fielden.platform.ioc.HibernateConfigurationFactory.setAutoCommitSettings;

/// Tests the auto-commit settings applied by [HibernateConfigurationFactory].
///
public class HibernateConfigurationFactoryTest {

    private static final String HIKARI_AUTO_COMMIT = "hibernate.hikari.autoCommit";

    @Test
    public void pooled_connections_have_auto_commit_disabled_and_Hibernate_relies_on_it() {
        final Configuration cfg = new Configuration();
        setAutoCommitSettings(new Properties(), cfg);

        assertEquals("false", cfg.getProperty(AvailableSettings.AUTOCOMMIT));
        assertEquals("true", cfg.getProperty(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT));
        assertEquals("false", cfg.getProperty(HIKARI_AUTO_COMMIT));
    }

    /// HikariCP gives `hibernate.hikari.autoCommit` precedence over `hibernate.connection.autocommit`.
    /// A configuration may contain it without the application properties specifying it, as the configuration starts from JVM system properties and a `hibernate.properties` resource.
    /// Such a value, which the configuration contains before the auto-commit settings are applied, does not enable auto-commit on the connections HikariCP hands out.
    ///
    @Test
    public void auto_commit_for_HikariCP_already_in_the_configuration_does_not_enable_auto_commit() {
        final Configuration cfg = new Configuration();
        cfg.setProperty(HIKARI_AUTO_COMMIT, "true");
        setAutoCommitSettings(new Properties(), cfg);

        assertFalse(HikariConfigurationUtil.loadConfiguration(cfg.getProperties()).isAutoCommit());
    }

    @Test
    public void specifying_auto_commit_is_rejected() {
        assertSpecifyingIsRejected(AvailableSettings.AUTOCOMMIT, "false");
    }

    /// Hibernate relies on `hibernate.connection.provider_disables_autocommit` without checking,
    /// so a value that does not match the connections handed out would either make each statement commit on its own, or reintroduce an exchange with the database when beginning a transaction.
    ///
    @Test
    public void specifying_provider_disables_autocommit_is_rejected() {
        assertSpecifyingIsRejected(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT, "true");
    }

    @Test
    public void specifying_auto_commit_for_HikariCP_is_rejected() {
        assertSpecifyingIsRejected(HIKARI_AUTO_COMMIT, "false");
    }

    private static void assertSpecifyingIsRejected(final String property, final String value) {
        final Properties props = new Properties();
        props.setProperty(property, value);

        final var ex = assertThrows(InvalidArgumentException.class, () -> setAutoCommitSettings(props, new Configuration()));
        assertEquals(ERR_AUTOCOMMIT_SETTING_SPECIFIED.formatted(property), ex.getMessage());
    }

}
