package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IUserPreferencesProvider;
import ua.com.fielden.platform.web.interfaces.impl.NoOpUserPreferencesProvider;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.resources.webui.ApplicationConfigurationResource.buildConfiguration;

/// Tests for [ApplicationConfigurationResource#buildConfiguration].
///
public class ApplicationConfigurationResourceTest extends AbstractWebResourceWithDaoTestCase {

    @Inject private IWebUiConfig webUiConfig;
    @Inject private IApplicationSettings appSettings;
    @Inject private IDates dates;
    @Inject private IUserProvider userProvider;

    @Test
    public void user_preferences_override_matching_standard_settings() {
        final IUserPreferencesProvider overridingProvider = user -> Map.of(
                "title", "Custom Title",
                "watermark", "Custom Watermark");

        final var config = buildConfiguration(webUiConfig, appSettings, dates, overridingProvider, userProvider.getUser());

        assertEquals("Custom Title", config.get("title"));
        assertEquals("Custom Watermark", config.get("watermark"));
    }

    @Test
    public void user_preferences_can_add_custom_settings() {
        final IUserPreferencesProvider customProvider = user -> Map.of("customKey", "customValue");

        final var config = buildConfiguration(webUiConfig, appSettings, dates, customProvider, userProvider.getUser());

        assertEquals("customValue", config.get("customKey"));
        assertEquals(webUiConfig.title(), config.get("title"));
    }

    @Test
    public void buildConfiguration_handles_null_user() {
        final var config = buildConfiguration(webUiConfig, appSettings, dates, new NoOpUserPreferencesProvider(), null);

        assertEquals(webUiConfig.title(), config.get("title"));
    }

}