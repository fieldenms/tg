package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/// A web-accessible resource that provides configuration data required by a web application at runtime.
/// The configuration is consumed by frontend clients during application startup or refresh
/// to dynamically adjust behaviour without requiring redeployment.
///
public class ApplicationConfigurationResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERR_DEVICE_SCREEN_WIDTH = "The desktop width can not be less then or equal tablet width.";

    private final RestServerUtil restUtil;

    private final IWebUiConfig webUiConfig;
    private final IApplicationSettings appSettings;
    private final IDates dates;

    public ApplicationConfigurationResource (
            final RestServerUtil restUtil,

            final IWebUiConfig webUiConfig,
            final IApplicationSettings appSettings,
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
        this.dates = dates;
    }

    /// Handles a GET request for configuration data.
    ///
    @Get
    public Representation get() {
        if (webUiConfig.minDesktopWidth() <= webUiConfig.minTabletWidth()) {
            LOGGER.error(ERR_DEVICE_SCREEN_WIDTH);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.webApiResultRepresentation(CollectionUtil.linkedMapOf(T2.t2("errorMsg", ERR_DEVICE_SCREEN_WIDTH)));
        }
        final Map<String, Object> configs = new LinkedHashMap<>();
        configs.put("siteAllowlist", webUiConfig.siteAllowList());
        configs.put("daysUntilSitePermissionExpires", webUiConfig.daysUntilSitePermissionExpires());
        configs.put("currencySymbol", appSettings.currencySymbol());
        configs.put("timeZone", webUiConfig.independentTimeZone() ? TimeZone.getDefault().getID() : "");
        configs.put("minDesktopWidth", webUiConfig.minDesktopWidth());
        configs.put("minTabletWidth", webUiConfig.minTabletWidth());
        configs.put("locale", webUiConfig.locale());
        configs.put("dateFormat", webUiConfig.dateFormat());
        configs.put("timeFormat", webUiConfig.timeFormat());
        configs.put("masterActionOptions", webUiConfig.masterActionOptions());
        configs.put("timeWithMillisFormat", webUiConfig.timeWithMillisFormat());
        // Need to set the first day of week, which is used by the date picker component to correctly render a weekly representation of a month.
        // Because IDates use a number range from 1 to 7 to represent Mon to Sun and JS uses 0 for Sun, we need to convert the value coming from IDates.
        configs.put("firstDayOfWeek", dates.startOfWeek() % 7);
        configs.put("title", webUiConfig.title());
        configs.put("ideaUri", webUiConfig.ideaUri());
        configs.put("panelColor", webUiConfig.mainPanelColor());
        configs.put("watermark", webUiConfig.watermark());
        configs.put("watermarkStyle", webUiConfig.watermarkStyle());
        return restUtil.webApiResultRepresentation(configs);
    }
}
