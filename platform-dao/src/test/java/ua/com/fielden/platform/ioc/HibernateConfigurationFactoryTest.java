package ua.com.fielden.platform.ioc;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.ioc.HibernateConfigurationFactory.ERR_AUTOCOMMIT_SETTING_SPECIFIED;
import static ua.com.fielden.platform.ioc.HibernateConfigurationFactory.setAutoCommitSettings;

/// Tests the auto-commit settings applied by [HibernateConfigurationFactory].
///
public class HibernateConfigurationFactoryTest {

    @Test
    public void pooled_connections_have_auto_commit_disabled_and_Hibernate_relies_on_it() {
        final Configuration cfg = new Configuration();
        setAutoCommitSettings(new Properties(), cfg);

        assertEquals("false", cfg.getProperty(AvailableSettings.AUTOCOMMIT));
        assertEquals("true", cfg.getProperty(AvailableSettings.CONNECTION_PROVIDER_DISABLES_AUTOCOMMIT));
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

    private static void assertSpecifyingIsRejected(final String property, final String value) {
        final Properties props = new Properties();
        props.setProperty(property, value);

        final var ex = assertThrows(InvalidArgumentException.class, () -> setAutoCommitSettings(props, new Configuration()));
        assertEquals(ERR_AUTOCOMMIT_SETTING_SPECIFIED.formatted(property), ex.getMessage());
    }

}
