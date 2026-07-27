package ua.com.fielden.platform.web.resources.webui;

import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.interfaces.IUserPreferencesProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import java.util.LinkedHashMap;
import java.util.TimeZone;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;

/// A web resource that exposes the client-side application configuration.
/// Extend this resource when additional platform-level configuration settings are needed.
///
/// Frontend clients consume this resource during startup or refresh to
/// dynamically adjust behavior without requiring a redeployment.
///
public class ApplicationConfigurationResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERR_DEVICE_SCREEN_WIDTH = "The desktop width cannot be less than or equal to the tablet width.";

    private final RestServerUtil restUtil;

    private final IWebUiConfig webUiConfig;
    private final IApplicationSettings appSettings;
    private final IUserPreferencesProvider userPreferencesProvider;
    private final IUserProvider userProvider;

    public ApplicationConfigurationResource (
            final RestServerUtil restUtil,

            final IWebUiConfig webUiConfig,
            final IApplicationSettings appSettings,
            final IUserPreferencesProvider userPreferencesProvider,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,

            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);

        this.restUtil = restUtil;
        this.webUiConfig = webUiConfig;
        this.appSettings = appSettings;
        this.userPreferencesProvider = userPreferencesProvider;
        this.userProvider = userProvider;
    }

    /// Handles a GET request for configuration data.
    ///
    @Get
    public Representation get() {
        if (webUiConfig.minDesktopWidth() <= webUiConfig.minTabletWidth()) {
            LOGGER.error(ERR_DEVICE_SCREEN_WIDTH);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.webApiResultRepresentation(linkedMapOf(t2("errorMsg", ERR_DEVICE_SCREEN_WIDTH)));
        }
        return restUtil.webApiResultRepresentation(buildConfiguration(webUiConfig, appSettings, dates, userPreferencesProvider, userProvider.getUser()));
    }

    /// Builds the application configuration map by combining platform-level settings with user-specific preferences.
    /// User preferences (from [IUserPreferencesProvider]) are applied last, overriding any matching platform-level keys.
    ///
    static LinkedHashMap<String, Object> buildConfiguration(
            final IWebUiConfig webUiConfig,
            final IApplicationSettings appSettings,
            final IDates dates,
            final IUserPreferencesProvider userPreferencesProvider,
            final @Nullable User user)
    {
        final var configs = new LinkedHashMap<String, Object>();
        configs.put("siteAllowlist", webUiConfig.siteAllowList());
        configs.put("daysUntilSitePermissionExpires", webUiConfig.daysUntilSitePermissionExpires());
        configs.put("minDesktopWidth", webUiConfig.minDesktopWidth());
        configs.put("minTabletWidth", webUiConfig.minTabletWidth());
        configs.put("currencySymbol", appSettings.currencySymbol());
        configs.put("timeZone", webUiConfig.independentTimeZone() ? TimeZone.getDefault().getID() : "");
        configs.put("locale", webUiConfig.locale());
        configs.put("dateFormat", webUiConfig.dateFormat());
        configs.put("timeFormat", webUiConfig.timeFormat());
        configs.put("timeWithMillisFormat", webUiConfig.timeWithMillisFormat());
        configs.put("masterActionOptions", webUiConfig.masterActionOptions());
        // IDates uses 1–7 for Mon–Sun; JS date pickers use 0 for Sun, so convert accordingly.
        configs.put("firstDayOfWeek", dates.startOfWeek() % 7);
        configs.put("title", webUiConfig.title());
        configs.put("ideaUri", webUiConfig.ideaUri());
        configs.put("panelColor", webUiConfig.mainPanelColor());
        configs.put("watermark", webUiConfig.watermark());
        configs.put("watermarkStyle", webUiConfig.watermarkStyle());
        configs.putAll(userPreferencesProvider.getPreferencesFor(user));
        return configs;
    }

}
