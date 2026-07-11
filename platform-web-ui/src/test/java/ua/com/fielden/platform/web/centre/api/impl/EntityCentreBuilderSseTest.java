package ua.com.fielden.platform.web.centre.api.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.sse.IEventSource;

/// A test case for Entity Centre DSL configuration of SSE-driven auto-refreshes.
///
public class EntityCentreBuilderSseTest {

    /// A stub event source type, sufficient for Entity Centre DSL, which only needs a class token.
    private interface StubEventSource extends IEventSource {}

    @Test
    public void event_source_and_refresh_settings_are_absent_unless_configured() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class).addProp("desc").build();

        assertEquals(empty(), config.getEventSourceClass());
        assertEquals(empty(), config.getRefreshCountdown());
        assertEquals(empty(), config.getMinAutoRefreshInterval());
    }

    @Test
    public void min_auto_refresh_interval_and_countdown_are_reflected_in_configuration() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .hasEventSource(StubEventSource.class)
                .withMinAutoRefreshInterval(300)
                .withCountdownRefreshPrompt(5)
                .addProp("desc").build();

        assertEquals(of(StubEventSource.class), config.getEventSourceClass());
        assertEquals(of(300), config.getMinAutoRefreshInterval());
        assertEquals(of(5), config.getRefreshCountdown());
    }

    @Test
    public void min_auto_refresh_interval_can_be_configured_without_a_refresh_prompt() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .hasEventSource(StubEventSource.class)
                .withMinAutoRefreshInterval(60)
                .addProp("desc").build();

        assertEquals(of(60), config.getMinAutoRefreshInterval());
        assertEquals(empty(), config.getRefreshCountdown());
    }

    @Test
    public void non_positive_min_auto_refresh_interval_is_rejected() {
        for (final int seconds : new int[] {0, -1}) {
            try {
                centreFor(TgWorkOrder.class).hasEventSource(StubEventSource.class).withMinAutoRefreshInterval(seconds);
                fail("Configuration with a non-positive minimum auto-refresh interval should have failed.");
            } catch (final EntityCentreConfigurationException ex) {
                assertEquals("The minimum auto-refresh interval [%s] should be greater than zero.".formatted(seconds), ex.getMessage());
            }
        }
    }

}
